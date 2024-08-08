package space.happyniggersin.common.modules.automation.channels

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import space.happyniggersin.common.event.discord.DiscordEvent

class AttachmentsOnlyChannel(channelId: Long) : ChannelSetting(channelId, 0) {
    override fun execute(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.getCancelled()) return Mono.empty()

        if (event.event.message.attachments.size == 0) {
            event.setCancelled(true)
            return event.event.message.delete()
        }

        return Mono.empty()
    }
}