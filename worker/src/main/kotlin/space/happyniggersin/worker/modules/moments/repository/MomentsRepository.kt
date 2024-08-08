package space.happyniggersin.worker.modules.moments.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import space.happyniggersin.worker.modules.moments.entity.MomentsSettings

@Repository
interface MomentsRepository: ReactiveCrudRepository<MomentsSettings, Long> {
}