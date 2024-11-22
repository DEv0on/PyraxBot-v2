package com.dev0on.common.modules.tickets.entity

import discord4j.common.util.Snowflake
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("tickets")
class Ticket() {
    @Id
    var ownerId: Long = 0

    var channelId: Long = 0

    constructor(ownerId: Long) : this() {
        this.ownerId = ownerId
    }

    constructor(ownerId: Snowflake) : this() {
        this.ownerId = ownerId.asLong()
    }
}