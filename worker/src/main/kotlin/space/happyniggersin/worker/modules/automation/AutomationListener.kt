package space.happyniggersin.worker.modules.automation

import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import space.happyniggersin.common.modules.automation.service.AutomationService
import space.happyniggersin.worker.annotation.event.DiscordEventListener
import space.happyniggersin.worker.event.discord.DiscordEvent

@Component
class AutomationListener {
    @Autowired
    lateinit var automationService: AutomationService

    @DiscordEventListener(type = MessageCreateEvent::class, order = 1, cancellable = false)
    fun onMessageCreate(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.event.guildId.isEmpty || event.event.member.isEmpty || event.event.member.get().isBot) return Mono.empty()

        return automationService.getOrCreate(event.event.guildId.get().asLong())
            .flatMapIterable { list ->
                list.channelSettings
                    .filter { it.channelId == event.event.message.channelId.asLong() }
                    .toList()
            }
            .flatMap {
                it.execute(event.event)
            }
            .then()
    }
}