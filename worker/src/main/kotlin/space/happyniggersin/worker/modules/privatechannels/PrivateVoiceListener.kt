package space.happyniggersin.worker.modules.privatechannels

import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.spec.GuildMemberEditSpec
import discord4j.core.spec.VoiceChannelCreateSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import space.happyniggersin.common.annotation.event.DiscordEventListener
import space.happyniggersin.common.event.discord.DiscordEvent
import space.happyniggersin.common.modules.privatechannels.entity.PrivateChannelSettings
import space.happyniggersin.common.modules.privatechannels.service.PrivateChannelService

@Component
class PrivateVoiceListener {

    @Autowired
    private lateinit var privateChannelService: PrivateChannelService

    @Autowired
    private lateinit var gateway: GatewayDiscordClient

    @DiscordEventListener(type = VoiceStateUpdateEvent::class, order = 0, cancellable = false)
    fun onVoiceStateUpdate(event: DiscordEvent<VoiceStateUpdateEvent>): Mono<Void> {
        return privateChannelService.getOrCreateSettings(event.event.current.guildId)
            .flatMap {
                this.execute(event, it)
            }
    }

    fun execute(event: DiscordEvent<VoiceStateUpdateEvent>, settings: PrivateChannelSettings): Mono<Void> {
        val current = event.event.current
        val old = event.event.old
        if (current.channelId.isPresent
            && current.channelId.get() == Snowflake.of(settings.channelId)
            && (old.isEmpty
                    || (old.get().channelId.isPresent
                        && old.get().channelId.get() != Snowflake.of(settings.channelId))
                    )
        ) {

            return gateway.getGuildById(Snowflake.of(settings.guildId))
                .zipWith(privateChannelService.getOrCreateVoiceChannel(current.userId))
                .flatMap {
                    if (it.t2.exist) return@flatMap Mono.error(RuntimeException("Voice already exists!"))
                    it.t1.createVoiceChannel(
                        VoiceChannelCreateSpec.of(settings.voiceDefaultName)
                            .withParentId(Snowflake.of(settings.categoryId))
                    ).zipWith(Mono.just(it.t2))
                }
                .flatMap {
                    it.t2.exist = true
                    it.t2.channelId = it.t1.id.asLong()
                    privateChannelService.updateVoiceChannel(it.t2)
                }
                .zipWith(current.member)
                .flatMap {
                    it.t2.edit(
                        GuildMemberEditSpec.create()
                            .withNewVoiceChannelOrNull(Snowflake.of(it.t1.channelId))
                    )
                }
                .onErrorResume {
                    if (it.cause !is RuntimeException)
                        it.cause?.printStackTrace()
                    return@onErrorResume Mono.empty()
                }
                .then()
        }
        return Mono.empty()
    }
}