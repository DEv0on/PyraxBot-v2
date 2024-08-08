package space.happyniggersin.common.modules.automation.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import space.happyniggersin.common.modules.automation.entity.AutomationSettings
import space.happyniggersin.common.modules.automation.repository.AutomationRepository

@Service
class AutomationService {
    @Autowired
    private lateinit var automationService: AutomationRepository

    @Transactional
    fun getOrCreate(id: Long): Mono<AutomationSettings> {
        return automationService.findById(id)
            .switchIfEmpty(automationService.save(AutomationSettings(id)))
    }

    @Transactional
    fun save(automationSettings: AutomationSettings): Mono<AutomationSettings> {
        return automationService.save(automationSettings)
    }
}