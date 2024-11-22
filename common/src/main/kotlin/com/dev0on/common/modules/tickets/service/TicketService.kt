package com.dev0on.common.modules.tickets.service

import discord4j.common.util.Snowflake
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import com.dev0on.common.modules.tickets.entity.Ticket
import com.dev0on.common.modules.tickets.entity.TicketSettings
import com.dev0on.common.modules.tickets.repository.TicketRepository
import com.dev0on.common.modules.tickets.repository.TicketSettingsRepository

@Service
class TicketService {

    @Autowired
    private lateinit var ticketRepository: TicketRepository

    @Autowired
    private lateinit var ticketSettingsRepository: TicketSettingsRepository

    @Transactional
    fun getOrCreateSettings(guildId: Snowflake): Mono<TicketSettings> {
        return ticketSettingsRepository.findById(guildId.asLong())
            .switchIfEmpty(ticketSettingsRepository.save(TicketSettings(guildId)))
    }

    @Transactional
    fun getOrCreateTicket(ownerId: Snowflake): Mono<Ticket> {
        return ticketRepository.findById(ownerId.asLong())
            .switchIfEmpty(ticketRepository.save(Ticket(ownerId)))
    }

    @Transactional
    fun findTicketByChannelId(channelId: Snowflake): Mono<Ticket> {
        return ticketRepository.findByChannelId(channelId.asLong())
    }

    @Transactional
    fun updateTicket(ticket: Ticket): Mono<Ticket> {
        return ticketRepository.save(ticket)
    }

    @Transactional
    fun deleteTicket(ticket: Ticket): Mono<Void> {
        return ticketRepository.delete(ticket)
    }

    @Transactional
    fun deleteTicket(channelId: Snowflake): Mono<Void> {
        return ticketRepository.deleteByChannelId(channelId.asLong())
    }
}