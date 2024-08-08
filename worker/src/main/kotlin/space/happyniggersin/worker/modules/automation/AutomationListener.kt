package space.happyniggersin.worker.modules.automation

import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import space.happyniggersin.common.event.discord.DiscordEvent
import space.happyniggersin.common.modules.automation.service.AutomationService
import space.happyniggersin.common.annotation.event.DiscordEventListener

@Component
class AutomationListener {
    @Autowired
    lateinit var automationService: AutomationService

    @DiscordEventListener(type = MessageCreateEvent::class, order = 1, cancellable = true)
    fun onMessageCreate(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.event.guildId.isEmpty || event.event.member.isEmpty || event.event.member.get().isBot) return Mono.empty()

        return automationService.getOrCreate(event.event.guildId.get().asLong())
            .flatMapIterable { list ->
                list.channelSettings
                    .filter { it.channelId == event.event.message.channelId.asLong() }
                    .sortedBy { it.order }
                    .toList()
            }
            .flatMap {
                it.execute(event)
            }
            .then()
    }
}