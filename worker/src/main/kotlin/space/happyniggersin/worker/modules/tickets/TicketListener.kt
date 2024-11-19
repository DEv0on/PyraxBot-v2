package space.happyniggersin.worker.modules.tickets

import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.channel.TextChannelDeleteEvent
import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import discord4j.core.`object`.PermissionOverwrite
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import discord4j.core.`object`.entity.User
import discord4j.core.spec.InteractionReplyEditSpec
import discord4j.core.spec.MessageCreateSpec
import discord4j.rest.util.AllowedMentions
import discord4j.rest.util.Color
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import space.happyniggersin.common.annotation.event.DiscordEventListener
import space.happyniggersin.common.event.discord.DiscordEvent
import space.happyniggersin.common.exception.CommandInvokeException
import space.happyniggersin.common.modules.tickets.service.TicketService
import space.happyniggersin.common.utils.embedReply
import java.time.Duration
import java.util.concurrent.TimeoutException

@Component
class TicketListener {
    @Autowired
    private lateinit var ticketService: TicketService

    @Autowired
    private lateinit var client: GatewayDiscordClient

    @DiscordEventListener(type = TextChannelDeleteEvent::class, order = 0, cancellable = false)
    fun onChannelDelete(event: DiscordEvent<TextChannelDeleteEvent>): Mono<Void> {
        return ticketService.findTicketByChannelId(event.event.channel.id)
            .flatMap {
                ticketService.deleteTicket(it)
            }
    }

    @DiscordEventListener(type = ButtonInteractionEvent::class, order = 0, cancellable = false)
    fun onButtonInteraction(event: DiscordEvent<ButtonInteractionEvent>): Mono<Void> {
        if (!event.event.customId.startsWith("ticket"))
            return Mono.empty()

        val action = event.event.customId.split("-")[1]

        when (action) {
            "create" -> {
                if (event.event.interaction.guildId.isEmpty)
                    return Mono.empty()

                return handleCreateButton(event.event)
            }

            "close" -> {
                return handleCloseButton(event.event)
            }

            else -> return Mono.empty()
        }
    }

    fun handleCloseButton(event: ButtonInteractionEvent): Mono<Void> {
        return event.deferReply()
            .withEphemeral(true)
            .then(ticketService.getOrCreateSettings(event.interaction.guildId.get()))
            .flatMap {
                event.editReply(
                    InteractionReplyEditSpec.create()
                        .withEmbeds(
                            it.ticketCloseConfirmationEmbed.toOriginal()
                        )
                        .withComponents(
                            ActionRow.of(
                                Button.danger(
                                    "ticket-delete-confirm",
                                    it.ticketCloseConfirmButtonText
                                ),
                                Button.secondary(
                                    "ticket-delete-discard",
                                    it.ticketCloseDiscardButtonText
                                )
                            )
                        )
                )
            }
            .flatMap {
                client.on(ButtonInteractionEvent::class.java) { buttonEvent ->
                    if (!buttonEvent.customId.startsWith("ticket-delete"))
                        return@on Mono.empty()
                    if (buttonEvent.customId == "ticket-delete-confirm") {
                        return@on buttonEvent
                            .deferReply()
                            .withEphemeral(true)
                            .then(event.editReply(""))
                            .then(buttonEvent.interaction.channel)
                            .flatMap {
                                ticketService.deleteTicket(buttonEvent.interaction.channelId)
                                    .then(Mono.just(it))
                            }
                            .flatMap { it.delete() }
                    } else {
                        return@on buttonEvent.deferReply()
                            .withEphemeral(true)
                            .then(Mono.fromCallable {
                                if (buttonEvent.interaction.message.isEmpty) return@fromCallable Mono.empty()

                                return@fromCallable buttonEvent.interaction.message.get().delete()
                            })
                            .flatMap {
                                event.deleteReply()
                                    .then(buttonEvent.embedReply("Anulowano", ephemeral = true))
                            }
                    }
                }
                    .timeout(Duration.ofSeconds(30))
                    .onErrorResume(TimeoutException::class.java) {
                        event.deleteReply()
                    }
                    .then()
            }
    }

    fun handleCreateButton(event: ButtonInteractionEvent): Mono<Void> {
        val user = event.interaction.user

        return event.deferReply()
            .withEphemeral(true)
            .then(ticketService.getOrCreateSettings(event.interaction.guildId.get()))
            .flatMap {
                if (!it.enabled || it.categoryId == -1L)
                    return@flatMap Mono.error(CommandInvokeException(event, "Module disabled"))
                else
                    return@flatMap Mono.just(it)
            }
            .zipWith(ticketService.getOrCreateTicket(user.id))
            .flatMap {
                if (it.t2.channelId != 0L)
                    return@flatMap Mono.error(CommandInvokeException(event, "Ticket already created"))
                else
                    return@flatMap Mono.just(it)
            }
            .zipWhen { event.interaction.guild }
            .flatMap { tuple ->
                val guild = tuple.t2
                val settings = tuple.t1.t1
                val ticket = tuple.t1.t2
                return@flatMap guild.createTextChannel(
                    getChannelName(
                        settings.channelName,
                        user
                    )
                )
                    .withParentId(Snowflake.of(settings.categoryId))
                    .flatMap { channel ->
                        ticket.channelId = channel.id.asLong()

                        ticketService.updateTicket(ticket)
                            .then(
                                channel.addMemberOverwrite(
                                    user.id, PermissionOverwrite.forMember(
                                        user.id, PermissionSet.of(
                                            Permission.VIEW_CHANNEL,
                                            Permission.SEND_MESSAGES
                                        ), PermissionSet.of()
                                    )
                                )
                            )
                            .then(
                                channel.createMessage(
                                    MessageCreateSpec.builder()
                                        .content(user.mention)
                                        .allowedMentions(
                                            AllowedMentions.builder().allowUser(user.id).build()
                                        )
                                        .embeds(
                                            settings.ticketEmbed.toOriginal()
                                        )
                                        .components(
                                            ActionRow.of(
                                                Button.danger(
                                                    "ticket-close-${user.id.asString()}",
                                                    settings.ticketCloseButtonText
                                                )
                                            )
                                        )
                                        .build()
                                )

                            )

                    }
                    .then(event.embedReply("Pomyślnie utworzono ticket", Color.GREEN))
            }
    }

    fun getChannelName(format: String, user: User): String {
        return format.replace("{user}", user.username)
    }
}