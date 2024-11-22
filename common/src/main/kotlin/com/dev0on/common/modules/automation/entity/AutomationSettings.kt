package com.dev0on.common.modules.automation.entity

import org.springframework.data.mongodb.core.mapping.Document
import com.dev0on.common.data.AbstractGuildSettings
import com.dev0on.common.modules.automation.channels.ChannelSetting

@Document(collection = "automation_settings")
class AutomationSettings() : AbstractGuildSettings() {

    var channelSettings: MutableList<ChannelSetting> = mutableListOf()

    constructor(guildId: Long) : this() {
        this.guildId = guildId
    }
}