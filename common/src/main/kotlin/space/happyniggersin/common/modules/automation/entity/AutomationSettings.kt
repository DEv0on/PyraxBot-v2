package space.happyniggersin.common.modules.automation.entity

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.reaction.ReactionEmoji
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import space.happyniggersin.common.modules.automation.types.ChannelSetting

@Document(collection = "automation_settings")
class AutomationSettings() {

    @Id
    var guildId: Long = 0

    var channelSettings: MutableList<ChannelSetting> = mutableListOf()

    constructor(guildId: Long): this() {
        this.guildId = guildId
    }
}