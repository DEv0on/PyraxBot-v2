package com.dev0on.common.modules.automation.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import com.dev0on.common.modules.automation.entity.AutomationSettings

@Repository
interface AutomationRepository: ReactiveCrudRepository<AutomationSettings, Long>