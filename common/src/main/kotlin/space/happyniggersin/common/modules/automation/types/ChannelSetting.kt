package space.happyniggersin.common.modules.automation.types

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

abstract class ChannelSetting(val channelId: Long) {
    abstract fun execute(event: MessageCreateEvent): Mono<Void>
}