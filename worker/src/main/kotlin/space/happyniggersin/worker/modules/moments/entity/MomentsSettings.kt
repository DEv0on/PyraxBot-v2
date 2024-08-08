package space.happyniggersin.worker.modules.moments.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "moments")
class MomentsSettings() {

    @Id
    var id: Long = 0

    var enabled: Boolean = false
    var channelId: Long = 0
    var emote: String = "⭐"

    constructor(id: Long) : this() {
        this.id = id
    }
}