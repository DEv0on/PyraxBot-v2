package space.happyniggersin.common.modules.tickets.data

import discord4j.core.spec.EmbedCreateSpec

class DatabaseEmbedData() {

    var title: String = ""
    var description: String = ""

    constructor(title: String, description: String) : this() {
        this.title = title
        this.description = description
    }

    fun toEmbed(): EmbedCreateSpec = EmbedCreateSpec.create()
        .withTitle(title)
        .withDescription(description)
}