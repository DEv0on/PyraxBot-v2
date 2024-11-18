package space.happyniggersin.common.exception

import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import reactor.core.publisher.Mono
import space.happyniggersin.common.utils.embedReply

class CommandInvokeException(val event: DeferrableInteractionEvent, reason: String): Exception(reason) {
    fun handleException(): Mono<Void> {
        val message = this.message ?: return event.embedReply("Unknown error")
        return event.embedReply(message)
    }
}