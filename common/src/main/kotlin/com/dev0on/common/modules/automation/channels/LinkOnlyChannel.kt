package com.dev0on.common.modules.automation.channels

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import com.dev0on.common.event.discord.DiscordEvent
import java.util.regex.Pattern

class LinkOnlyChannel(channelId: Long): ChannelSetting(channelId, 0) {
    companion object {
        private val URL_REGEX = Pattern.compile("(https?://(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}|https?://(?:www\\.|(?!www))[a-zA-Z0-9]+\\.\\S{2,}|www\\.[a-zA-Z0-9]+\\.\\S{2,})")
    }

    override fun execute(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.getCancelled()) return Mono.empty()

        val msg = event.event.message.content
        if (URL_REGEX.matcher(msg).matches()) {
            return Mono.empty()
        }

        event.setCancelled(true)
        return event.event.message.delete()
    }
}