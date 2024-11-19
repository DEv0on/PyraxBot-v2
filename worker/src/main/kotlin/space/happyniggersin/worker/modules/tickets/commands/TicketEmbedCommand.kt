package space.happyniggersin.worker.modules.tickets.commands

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionReplyEditSpec
import discord4j.core.spec.MessageCreateSpec
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono
import space.happyniggersin.common.annotation.command.Command
import space.happyniggersin.common.exception.CommandInvokeException
import space.happyniggersin.common.modules.tickets.service.TicketService
import space.happyniggersin.common.utils.checkRequiredRoles

@Command
class TicketEmbedCommand {

    @Autowired
    private lateinit var ticketService: TicketService

    @Command("ticketembed")
    fun ticketEmbed(event: ChatInputInteractionEvent): Mono<Void> {
        if (event.interaction.guildId.isEmpty)
            return Mono.empty()

        return event.deferReply()
            .withEphemeral(true)
            .then(ticketService.getOrCreateSettings(event.interaction.guildId.get()))
            .flatMap { event.checkRequiredRoles(it, it.ticketCommandRolesWithPermission) }
            .zipWhen {
                if (!it.enabled || it.categoryId == -1L || it.embedChannelId == 0L)
                    return@zipWhen Mono.error(CommandInvokeException(event, "Module disabled"))

                return@zipWhen event.client.getChannelById(Snowflake.of(it.embedChannelId)).ofType(TextChannel::class.java)
            }
            .flatMap { tuple ->
                val channel = tuple.t2
                val settings = tuple.t1
                channel.createMessage(
                    MessageCreateSpec.builder()
                        .embeds(settings.createEmbed.toOriginal())
                        .components(ActionRow.of(Button.primary("ticket-create", settings.createButtonText)))
                        .build()
                )
                    .then(
                        event.editReply(
                            InteractionReplyEditSpec.create()
                                .withEmbeds(
                                    EmbedCreateSpec.builder().description("Wysłano embed do tworzenia ticketów").build()
                                )

                        )
                    )
                    .then()
            }
    }
}