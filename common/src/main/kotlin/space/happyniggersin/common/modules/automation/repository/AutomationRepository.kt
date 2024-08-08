package space.happyniggersin.common.modules.automation.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import space.happyniggersin.common.modules.automation.entity.AutomationSettings

@Repository
interface AutomationRepository: ReactiveCrudRepository<AutomationSettings, Long>