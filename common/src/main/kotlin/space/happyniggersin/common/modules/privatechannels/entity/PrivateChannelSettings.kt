package space.happyniggersin.common.modules.privatechannels.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class PrivateChannelSettings() {
    @Id
    var guildId: Long = 0

    var categoryId: Long = 0
    var channelId: Long = 0
    var voiceDefaultName: String = "Kanał %user%"

    constructor(guildId: Long): this() {
        this.guildId = guildId
    }
}