package space.happyniggersin.worker.event.discord

import discord4j.core.event.domain.Event
import reactor.core.publisher.Mono
import java.lang.reflect.Method

class ListenerDefinition(val method: Method, val bean: Any, val order: Int, private val cancellable: Boolean) {
    fun call(event: DiscordEvent<Event>): Mono<Void> {
        if (cancellable) return Mono.empty()
        return method.invoke(bean, event) as Mono<Void>
    }
}