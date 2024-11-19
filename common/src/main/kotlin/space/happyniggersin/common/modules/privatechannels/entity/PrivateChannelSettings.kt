package space.happyniggersin.common.modules.privatechannels.entity

import discord4j.core.`object`.entity.User
import org.springframework.data.mongodb.core.mapping.Document
import space.happyniggersin.common.data.AbstractGuildSettings

@Document
class PrivateChannelSettings() : AbstractGuildSettings() {

    var categoryId: Long = 0
    var channelId: Long = 0
    var voiceDefaultName: String = "Kanał %user%"

    constructor(guildId: Long) : this() {
        this.guildId = guildId
    }

    fun getName(user: User): String {
        return this.voiceDefaultName
            .replace("%user%", user.username)
    }
}