package space.happyniggersin.worker.test

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionReplyEditSpec
import discord4j.rest.util.Color
import reactor.core.publisher.Mono
import space.happyniggersin.worker.annotation.command.Command

@Command
class PingCommand {
    @Command(name = "ping")
    fun ping(event: ChatInputInteractionEvent): Mono<Void> {
        return event.deferReply().then(
            event.editReply(
                InteractionReplyEditSpec.builder()
                    .addEmbed(
                        EmbedCreateSpec.builder()
                            .color(Color.RED)
                            .description("pong")
                            .build()
                    )
                    .build()
            )
        ).then()
    }
}