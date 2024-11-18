package space.happyniggersin.common.modules.tickets.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import space.happyniggersin.common.modules.tickets.entity.TicketSettings

interface TicketSettingsRepository : ReactiveCrudRepository<TicketSettings, Long>