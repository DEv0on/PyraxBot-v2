package com.dev0on.common.modules.automation.channels

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.spec.EmbedCreateFields
import discord4j.core.spec.EmbedCreateSpec
import reactor.core.publisher.Mono
import com.dev0on.common.event.discord.DiscordEvent

class ConvertToEmbedChannel(channelId: Long) : ChannelSetting(channelId, 3) {
    override fun execute(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.getCancelled() || event.event.message.author.isEmpty) return Mono.empty()
        val message = event.event.message

        return message.delete()
            .then(message.channel)
            .ofType(TextChannel::class.java)
            .flatMap {
                it.createMessage(
                    EmbedCreateSpec.create()
                        .withAuthor(EmbedCreateFields.Author.of(message.author.get().username, null, message.author.get().defaultAvatarUrl))
                        .withDescription(message.content)
                )
            }
            .then()
    }
}