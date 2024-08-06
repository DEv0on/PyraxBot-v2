package space.happyniggersin.worker.modules.moments

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.reaction.ReactionEmoji
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import space.happyniggersin.worker.annotation.event.DiscordEventListener
import space.happyniggersin.worker.event.discord.DiscordEvent

@Component
class MomentsListener {
    @DiscordEventListener(type = MessageCreateEvent::class, order = 1, cancellable = false)
    fun onMessageCreate(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.event.member.isEmpty || event.event.member.get().isBot) return Mono.empty()
        if (event.event.message.channelId != Snowflake.of("1270440065603993631")) return Mono.empty()
        if (event.event.message.attachments.size == 0) return event.event.message.delete()


        //todo guild settings
        return event.event.message.addReaction(ReactionEmoji.unicode("⭐"))
    }
}