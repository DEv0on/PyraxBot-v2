package space.happyniggersin.common.modules.privatechannels.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import space.happyniggersin.common.modules.privatechannels.entity.PrivateChannel

@Component
interface PrivateChannelRepository: ReactiveCrudRepository<PrivateChannel, Long>