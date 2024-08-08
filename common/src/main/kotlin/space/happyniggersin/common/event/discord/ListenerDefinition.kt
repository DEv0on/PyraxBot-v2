package space.happyniggersin.common.event.discord

import discord4j.core.event.domain.Event
import reactor.core.publisher.Mono
import java.lang.reflect.Method

class ListenerDefinition(private val method: Method, private val bean: Any, val order: Int, val cancellable: Boolean) {
    fun call(event: DiscordEvent<Event>): Mono<Void> {
        if (event.getCancelled()) return Mono.empty()
        return method.invoke(bean, event) as Mono<Void>
    }
}