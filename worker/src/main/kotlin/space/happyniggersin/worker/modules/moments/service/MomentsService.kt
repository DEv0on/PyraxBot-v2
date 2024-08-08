package space.happyniggersin.worker.modules.moments.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import space.happyniggersin.worker.modules.moments.entity.MomentsSettings
import space.happyniggersin.worker.modules.moments.repository.MomentsRepository

@Service
class MomentsService(private val repo: MomentsRepository) {

    @Transactional
    fun getSettings(id: Long): Mono<MomentsSettings> {
        return repo.findById(id)
            .switchIfEmpty(repo.save(MomentsSettings(id)))
    }
}