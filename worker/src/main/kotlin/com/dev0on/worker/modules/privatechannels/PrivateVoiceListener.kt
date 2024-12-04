package com.dev0on.worker.modules.privatechannels

import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.event.domain.channel.VoiceChannelDeleteEvent
import discord4j.core.spec.GuildMemberEditSpec
import discord4j.core.spec.VoiceChannelCreateSpec
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.dev0on.common.annotation.event.DiscordEventListener
import com.dev0on.common.event.discord.DiscordEvent
import com.dev0on.common.modules.privatechannels.entity.PrivateChannelSettings
import com.dev0on.common.modules.privatechannels.service.PrivateChannelService
import com.dev0on.common.utils.hasJoinedChannel
import discord4j.core.`object`.entity.channel.AudioChannel

@Component
class PrivateVoiceListener {

    @Autowired
    private lateinit var privateChannelService: PrivateChannelService

    @Autowired
    private lateinit var gateway: GatewayDiscordClient

    private val logger = LoggerFactory.getLogger(PrivateVoiceListener::class.java)

    @DiscordEventListener(type = VoiceStateUpdateEvent::class, order = 0, cancellable = false)
    fun onVoiceStateUpdate(event: DiscordEvent<VoiceStateUpdateEvent>): Mono<Void> {
        return privateChannelService.getOrCreateSettings(event.event.current.guildId)
            .flatMap { settings ->
                if (event.event.isLeaveEvent) {
                    return@flatMap event.event.old.get().channel
                        .flatMap {
                            deleteChannel(event, settings, it)
                        }
                } else if (event.event.isMoveEvent) {
                    if (event.event.hasJoinedChannel(Snowflake.of(settings.channelId))) {
                        return@flatMap createChannel(event, settings)
                    } else {
                        return@flatMap privateChannelService.doesChannelExist(event.event.old.get().channelId.get())
                            .flatMap dce@{
                                if (it)
                                    return@dce event.event.old.get().channel.flatMap { ch ->
                                        deleteChannel(event, settings, ch)
                                    }
                                return@dce Mono.empty()
                            }
                    }
                } else if (event.event.hasJoinedChannel(Snowflake.of(settings.channelId))) {
                    return@flatMap createChannel(event, settings)
                }

                return@flatMap Mono.empty()
            }
    }

    @DiscordEventListener(type = VoiceChannelDeleteEvent::class, order = 0, cancellable = false)
    fun onVoiceChannelDelete(event: DiscordEvent<VoiceChannelDeleteEvent>): Mono<Void> {
        return privateChannelService.getVoiceChannelByChannelId(event.event.channel.id)
            .flatMap {
                it.exist = false
                privateChannelService.updateVoiceChannel(it)
            }
            .then()
    }

    fun deleteChannel(event: DiscordEvent<VoiceStateUpdateEvent>, settings: PrivateChannelSettings, channel: AudioChannel): Mono<Void> {
        return channel
            .voiceStates.count()
            .zipWith(privateChannelService.getVoiceChannelByChannelId(channel.id))
            .flatMap {
                if (it.t1 != 0L || it.t2.permanent) return@flatMap Mono.empty()

                it.t2.exist = false
                return@flatMap Flux.concat(
                    privateChannelService.updateVoiceChannel(it.t2).then(),
                    channel.delete().then()
                ).collectList().then()
            }
    }

    fun createChannel(event: DiscordEvent<VoiceStateUpdateEvent>, settings: PrivateChannelSettings): Mono<Void> {
        val current = event.event.current

        return gateway.getGuildById(Snowflake.of(settings.guildId))
            .zipWith(privateChannelService.getOrCreateVoiceChannel(current.userId))
            .zipWhen { current.member }
            .flatMap {
                if (it.t1.t2.exist)
                    return@flatMap it.t2.edit(
                        GuildMemberEditSpec.create()
                            .withNewVoiceChannelOrNull(Snowflake.of(it.t1.t2.channelId))
                    )
                        .then(Mono.error(RuntimeException("Voice already exists!")))
                it.t1.t1.createVoiceChannel(
                    VoiceChannelCreateSpec.of(
                        settings.getName(it.t2)
                    )
                        .withParentId(Snowflake.of(settings.categoryId))
                ).zipWith(Mono.just(it.t1.t2))
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
                if (it !is RuntimeException)
                    logger.info("Caught error:", it)
                return@onErrorResume Mono.empty()
            }
            .then()
    }
}