package space.happyniggersin.worker

import discord4j.core.event.ReactiveEventAdapter
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import space.happyniggersin.common.event.command.CommandEventListenerFactory
import space.happyniggersin.common.event.discord.DiscordEvent
import space.happyniggersin.common.event.discord.EventBeanPostProcess

@Component
class EventHandler : ReactiveEventAdapter() {

    @Autowired
    private lateinit var commandFactory: CommandEventListenerFactory

    @Autowired
    private lateinit var eventFactory: EventBeanPostProcess

    override fun hookOnEvent(event: Event): Publisher<Void> {
        if (event is ChatInputInteractionEvent) {
            return commandFactory.invoke(event)
        } else {
            val listeners = eventFactory.registeredEvents.getOrDefault(event::class, listOf())
            val dEvent = DiscordEvent(event, listeners.any { it.cancellable })

            return Flux.concat(
                listeners
                    .sortedBy { it.order }
                    .map { listener ->
                        return@map listener.call(dEvent)
                    })
        }
    }
}