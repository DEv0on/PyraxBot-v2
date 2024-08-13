package space.happyniggersin.common.modules.privatechannels.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class PrivateChannel() {
    @Id
    var userId: Long = 0
    var channelId: Long = 0

    var exist: Boolean = false
    var permanent: Boolean = false

    constructor(userId: Long): this() {
        this.userId = userId
    }
}