package space.happyniggersin.common.modules.tickets.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import space.happyniggersin.common.modules.tickets.entity.Ticket

interface TicketRepository : ReactiveCrudRepository<Ticket, Long> {
    fun findByChannelId(channelId: Long): Mono<Ticket>
    fun deleteByChannelId(channelId: Long): Mono<Void>
}
