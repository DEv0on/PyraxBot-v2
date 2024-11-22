package com.dev0on.common.modules.automation.channels

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.StartThreadSpec
import reactor.core.publisher.Mono
import com.dev0on.common.event.discord.DiscordEvent

class AutoThreadChannel(channelId: Long, private val name: String) : ChannelSetting(channelId, 2) {
    override fun execute(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.getCancelled()) return Mono.empty()

        return event.event.message
            .startThread(StartThreadSpec.of(name))
            .then()
    }
}