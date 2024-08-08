package space.happyniggersin.common.modules.automation.channels

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.reaction.ReactionEmoji
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import space.happyniggersin.common.event.discord.DiscordEvent

class AutoReactionChannel(channelId: Long) : ChannelSetting(channelId, 1) {
    var emotes: MutableList<Emoji> = mutableListOf()

    override fun execute(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.getCancelled()) return Mono.empty()

        return Flux.fromIterable(emotes.asIterable())
            .flatMap { emote ->
                event.event.message.addReaction(emote.getEmojiData())
            }
            .then()
    }

    class Emoji() {
        var id: Long = 0
        var name: String = ""
        var isAnimated: Boolean = false
        var isUnicode: Boolean = false

        constructor(id: Long, name: String, isAnimated: Boolean): this() {
            this.id = id
            this.name = name
            this.isAnimated = isAnimated
            this.isUnicode = false
        }

        constructor(unicode: String): this() {
            this.name = unicode
            this.isUnicode = true
        }

        fun getEmojiData(): ReactionEmoji {
            return if (isUnicode)
                ReactionEmoji.unicode(this.name)
            else
                ReactionEmoji.custom(
                    Snowflake.of(this.id),
                    this.name,
                    this.isAnimated
                )
        }
    }
}