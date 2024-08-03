package space.happyniggersin.worker

import discord4j.core.event.ReactiveEventAdapter
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.util.Loggers
import space.happyniggersin.worker.event.command.CommandEventListenerFactory

@Component
class EventHandler: ReactiveEventAdapter() {

    companion object {
        private val log = Loggers.getLogger(EventHandler::class.java)
    }

    @Autowired
    private lateinit var commandFactory: CommandEventListenerFactory

    override fun onChatInputInteraction(event: ChatInputInteractionEvent): Publisher<*> {
        return commandFactory.invoke(event)
    }

    override fun onMessageCreate(event: MessageCreateEvent): Publisher<*> {
        return super.onMessageCreate(event)
    }

    override fun onReady(event: ReadyEvent): Publisher<*> {
        log.info(
            "Shard [{}] logged in as {}",
            event.shardInfo,
            event.self.username
        )
        return super.onReady(event)
    }
}