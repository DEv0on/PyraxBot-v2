package space.happyniggersin.worker.modules.proposition

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.spec.EmbedCreateFields
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Color
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import space.happyniggersin.worker.annotation.event.DiscordEventListener
import space.happyniggersin.worker.event.discord.DiscordEvent

@Component
class PropositionListener {
    @DiscordEventListener(type = MessageCreateEvent::class, order = 1, cancellable = false)
    fun onMessageCreate(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.event.member.isEmpty || event.event.member.get().isBot) return Mono.empty()
        if (event.event.message.channelId != Snowflake.of("1270426633458942013")) return Mono.empty()

        //todo: guild setings for embed layout and channel id

        val author = event.event.message.author.get().username
        val authorIcon = event.event.message.author.get().avatarUrl
        val message = event.event.message.content
        if (event.event.message.author.isEmpty) return Mono.empty()

        return event.event.message.delete()
            .then(event.event.message.channel)
            .ofType(TextChannel::class.java)
            .flatMap { channel ->
                channel.createMessage(
                    EmbedCreateSpec.create()
                        .withColor(Color.GREEN)
                        .withAuthor(EmbedCreateFields.Author.of(author, null, authorIcon))
                        .withDescription(message)
                )
            }
            .flatMap { msg ->

                Mono.`when`(
                    msg.addReaction(ReactionEmoji.unicode("✅")),
                    msg.addReaction(ReactionEmoji.unicode("❌"))
                )
            }
            .then()
    }
}