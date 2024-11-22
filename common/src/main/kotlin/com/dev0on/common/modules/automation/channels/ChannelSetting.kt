package com.dev0on.common.modules.automation.channels

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import com.dev0on.common.event.discord.DiscordEvent

abstract class ChannelSetting(var channelId: Long, var order: Int) {
    abstract fun execute(event: DiscordEvent<MessageCreateEvent>): Mono<Void>
}