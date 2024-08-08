package space.happyniggersin.common.modules.automation.channels

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import space.happyniggersin.common.event.discord.DiscordEvent

abstract class ChannelSetting(var channelId: Long, var order: Int) {
    abstract fun execute(event: DiscordEvent<MessageCreateEvent>): Mono<Void>
}