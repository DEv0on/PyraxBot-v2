package com.dev0on.common.modules.tickets.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import com.dev0on.common.modules.tickets.entity.TicketSettings

interface TicketSettingsRepository : ReactiveCrudRepository<TicketSettings, Long>