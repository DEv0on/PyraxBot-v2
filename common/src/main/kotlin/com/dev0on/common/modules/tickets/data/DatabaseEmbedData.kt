package com.dev0on.common.modules.tickets.data

import discord4j.core.spec.EmbedCreateFields.Author
import discord4j.core.spec.EmbedCreateFields.Field
import discord4j.core.spec.EmbedCreateFields.Footer
import discord4j.core.spec.EmbedCreateSpec
import discord4j.discordjson.possible.Possible
import discord4j.rest.util.Color
import java.time.Instant

class DatabaseEmbedData() : Convertable<EmbedCreateSpec> {

    private var title: String? = null
    private var description: String? = null
    private var url: String? = null
    private var timestamp: Instant? = null
    private var color: Color? = null
    private var image: String? = null
    private var thumbnail: String? = null
    private var footer: FooterData? = null
    private var author: AuthorData? = null
    private var fields: List<FieldData> = arrayListOf()


    fun withTitle(title: String?): DatabaseEmbedData {
        this.title = title
        return this
    }

    fun withDescription(description: String?): DatabaseEmbedData {
        this.description = description
        return this
    }

    fun withUrl(url: String?): DatabaseEmbedData {
        this.url = url
        return this
    }

    fun withTimestamp(timestamp: Instant?): DatabaseEmbedData {
        this.timestamp = timestamp
        return this
    }

    fun withColor(color: Color?): DatabaseEmbedData {
        this.color = color
        return this
    }

    fun withImage(image: String?): DatabaseEmbedData {
        this.image = image
        return this
    }

    fun withThumbnail(thumbnail: String?): DatabaseEmbedData {
        this.thumbnail = thumbnail
        return this
    }

    fun withFooter(footer: FooterData?): DatabaseEmbedData {
        this.footer = footer
        return this
    }

    fun withAuthor(author: AuthorData?): DatabaseEmbedData {
        this.author = author
        return this
    }

    fun withFields(fields: List<FieldData>): DatabaseEmbedData {
        this.fields = fields
        return this
    }

    class FooterData(private var text: String, private var iconUrl: String?) : Convertable<Footer> {
        override fun toOriginal(): Footer {
            return Footer.of(text, iconUrl)
        }
    }

    class AuthorData(private var name: String, private var url: String?, private var iconUrl: String?) :
        Convertable<Author> {
        override fun toOriginal(): Author {
            return Author.of(name, url, iconUrl)
        }
    }

    class FieldData(private var name: String, private var value: String, private var inline: Boolean) :
        Convertable<Field> {

        override fun toOriginal(): Field {
            return Field.of(name, value, inline)
        }
    }

    override fun toOriginal(): EmbedCreateSpec =
        EmbedCreateSpec.create()
            .withTitle(possibleFromNullable(title))
            .withDescription(possibleFromNullable(description))
            .withUrl(possibleFromNullable(url))
            .withTimestamp(possibleFromNullable(timestamp))
            .withColor(possibleFromNullable(color))
            .withImage(possibleFromNullable(image))
            .withThumbnail(possibleFromNullable(thumbnail))
            .withFooter(possibleFromNullable(footer).toOptional().map { it.toOriginal() }.orElse(null))
            .withAuthor(possibleFromNullable(author).toOptional().map { it.toOriginal() }.orElse(null))
            .withFields(fields.map { it.toOriginal() })
}

interface Convertable<T : Any> {
    fun toOriginal(): T
}

fun <T : Any> possibleFromNullable(item: T?): Possible<T> {
    return if (item == null) Possible.absent() else Possible.of(item)
}