package space.happyniggersin.worker.modules.moments

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.reaction.ReactionEmoji
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import space.happyniggersin.worker.annotation.event.DiscordEventListener
import space.happyniggersin.worker.event.discord.DiscordEvent
import space.happyniggersin.worker.modules.moments.entity.MomentsSettings
import space.happyniggersin.worker.modules.moments.repository.MomentsRepository
import space.happyniggersin.worker.modules.moments.service.MomentsService

@Component
class MomentsListener {

    @Autowired
    lateinit var momentsService: MomentsService

    @DiscordEventListener(type = MessageCreateEvent::class, order = 1, cancellable = false)
    fun onMessageCreate(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.event.guildId.isEmpty || event.event.member.isEmpty || event.event.member.get().isBot) return Mono.empty()
        if (event.event.message.attachments.size == 0) return event.event.message.delete()

        //todo rewrite to automation as autoemoji + autothreads


        return momentsService.getSettings(event.event.guildId.get().asLong())
            .flatMap { execute(event.event, it) }
            .then()
    }

    fun execute(event: MessageCreateEvent, settings: MomentsSettings): Mono<Void> {
        if (!settings.enabled) return Mono.empty()
        if (event.message.channelId != Snowflake.of(settings.channelId)) return Mono.empty()

        return event.message.addReaction(ReactionEmoji.unicode(settings.emote))
    }

}