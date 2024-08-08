package space.happyniggersin.common.modules.automation.channels

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.StartThreadSpec
import reactor.core.publisher.Mono
import space.happyniggersin.common.event.discord.DiscordEvent

class AutoThreadChannel(channelId: Long, private val name: String) : ChannelSetting(channelId, 2) {
    override fun execute(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.getCancelled()) return Mono.empty()

        return event.event.message
            .startThread(StartThreadSpec.of(name))
            .then()
    }
}