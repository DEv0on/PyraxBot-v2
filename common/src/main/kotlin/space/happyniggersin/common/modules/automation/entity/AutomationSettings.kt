package space.happyniggersin.common.modules.automation.entity

import org.springframework.data.mongodb.core.mapping.Document
import space.happyniggersin.common.data.AbstractGuildSettings
import space.happyniggersin.common.modules.automation.channels.ChannelSetting

@Document(collection = "automation_settings")
class AutomationSettings() : AbstractGuildSettings() {

    var channelSettings: MutableList<ChannelSetting> = mutableListOf()

    constructor(guildId: Long) : this() {
        this.guildId = guildId
    }
}