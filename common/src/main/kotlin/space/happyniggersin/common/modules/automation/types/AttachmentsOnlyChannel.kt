package space.happyniggersin.common.modules.automation.types

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class AttachmentsOnlyChannel(channelId: Long) : ChannelSetting(channelId) {
    override fun execute(event: MessageCreateEvent): Mono<Void> {
        if (event.message.attachments.size == 0)
            return event.message.delete()
        return Mono.empty()
    }
}