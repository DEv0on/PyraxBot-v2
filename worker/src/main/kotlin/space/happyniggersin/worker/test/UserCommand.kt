package space.happyniggersin.worker.test

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.entity.User
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
        fun addUser(
            event: ChatInputInteractionEvent,
            @Option("reason") reason: Optional<String>,
            @Option("user") user: Optional<Mono<User>>,
            @Option("role") role: Optional<String>
        ): Mono<Void> {
            return Mono.empty()
        }
    }
}
///user admin add-role reason:chuj user:chuj role:chuj