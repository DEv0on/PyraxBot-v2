package space.happyniggersin.common.modules.automation.channels

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import space.happyniggersin.common.event.discord.DiscordEvent

class LastLetterPrevMessageChannel(channelId: Long) : ChannelSetting(channelId, 0) {
    override fun execute(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.getCancelled()) return Mono.empty()

        val message = event.event.message

        return event.event.message.channel
            .flatMap {
                it.lastMessage
            }
            .flatMap {
                if (it.content[it.content.length-1] != message.content.first())
                    return@flatMap message.delete()
                return@flatMap Mono.empty()
            }
            .then()
    }
}