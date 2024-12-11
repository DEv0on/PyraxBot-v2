package com.dev0on.common.modules.automation.channels

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import com.dev0on.common.event.discord.DiscordEvent
import java.util.regex.Pattern

class LinkOnlyChannel(channelId: Long): ChannelSetting(channelId, 0) {
    companion object {
        val URL_REGEX = Pattern.compile("(?<protocol>http[s]?:\\/\\/)?(?<domain>[^\\/\\s]+)(?<path>\\/.*)")
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