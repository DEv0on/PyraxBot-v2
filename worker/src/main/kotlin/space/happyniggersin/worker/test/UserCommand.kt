package space.happyniggersin.worker.test

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionReplyEditSpec
import discord4j.rest.util.Color
import reactor.core.publisher.Mono
import space.happyniggersin.worker.annotation.command.Command
import space.happyniggersin.worker.annotation.command.Option
import space.happyniggersin.worker.annotation.command.Subcommand
import java.util.*

@Command("user")
class UserCommand {

    @Subcommand("admin")
    class Admin {

        @Command("add-role")
        fun addUser(event: ChatInputInteractionEvent,
                    @Option("reason") reason: Optional<String>,
                    @Option("user") user: Optional<Mono<User>>,
                    @Option("role") role: Optional<String>
        ): Mono<Void> {
            return event.deferReply().then(
                event.editReply(
                    InteractionReplyEditSpec.builder()
                        .addEmbed(
                            EmbedCreateSpec.builder()
                                .color(Color.RED)
                                .description("kys")
                                .build()
                        )
                        .build()
                )).then()
        }

    }
}