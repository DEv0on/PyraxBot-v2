package space.happyniggersin.worker.test

import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import space.happyniggersin.worker.annotation.event.DiscordEventListener
import space.happyniggersin.worker.event.discord.DiscordEvent

@Component
class TestEvent {
    @DiscordEventListener(type =  MessageCreateEvent::class, order = 1, cancellable = false)
    fun onMessageCreate(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.event.message.content != "penis") return Mono.empty()

        return event.event.message.channel
            .flatMap {
                it.createMessage("chuj")
            }
            .then()
    }
}