package space.happyniggersin.common.modules.tickets.entity

import discord4j.common.util.Snowflake
import discord4j.core.`object`.PermissionOverwrite
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import space.happyniggersin.common.modules.tickets.data.DatabaseEmbedData

@Document("ticket_settings")
class TicketSettings() {
    @Id
    var guildId: Long = 0

    var enabled: Boolean = false

    var categoryId: Long = -1

    var embedChannelId: Long = 0

    var createEmbed: DatabaseEmbedData =
        DatabaseEmbedData("Utwórz ticket!", "Aby otworzyć ticket naciśnij przycisk poniżej.")
    var createButtonText: String = "Utwórz ticket"

    var defaultPermissions: List<PermissionOverwrite> = emptyList()

    var ticketEmbed: DatabaseEmbedData = DatabaseEmbedData("Ticket", "Opisz swój problem")
    var ticketCloseButtonText: String = "Zamknij ticket"

    var ticketCloseConfirmationEmbed: DatabaseEmbedData =
        DatabaseEmbedData("Usuwanie ticketa", "Czy jesteś pewny, że chcesz usunąć ten ticket?")
    var ticketCloseConfirmButtonText: String = "Jestem pewny/a"
    var ticketCloseDiscardButtonText: String = "Jednak nie"

    var channelName: String = "Ticket {user}"

    constructor(guildId: Long) : this() {
        this.guildId = guildId
    }

    constructor(guildId: Snowflake) : this() {
        this.guildId = guildId.asLong()
    }


}