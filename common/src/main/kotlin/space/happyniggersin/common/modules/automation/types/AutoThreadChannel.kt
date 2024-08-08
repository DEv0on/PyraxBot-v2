package space.happyniggersin.common.modules.automation.types

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.StartThreadSpec
import reactor.core.publisher.Mono

class AutoThreadChannel(channelId: Long, val name: String) : ChannelSetting(channelId) {
    override fun execute(event: MessageCreateEvent): Mono<Void> {
        return event.message
            .startThread(StartThreadSpec.of(name))
            .then()
    }
}