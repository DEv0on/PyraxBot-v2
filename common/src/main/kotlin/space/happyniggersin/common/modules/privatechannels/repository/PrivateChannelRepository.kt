package space.happyniggersin.common.modules.privatechannels.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import space.happyniggersin.common.modules.privatechannels.entity.PrivateChannel

@Component
interface PrivateChannelRepository: ReactiveCrudRepository<PrivateChannel, Long> {
    fun findByChannelId(channelId: Long): Mono<PrivateChannel>
}