package com.dev0on.common.modules.privatechannels.service

import discord4j.common.util.Snowflake
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import com.dev0on.common.modules.privatechannels.entity.PrivateChannel
import com.dev0on.common.modules.privatechannels.entity.PrivateChannelSettings
import com.dev0on.common.modules.privatechannels.repository.PrivateChannelRepository
import com.dev0on.common.modules.privatechannels.repository.PrivateChannelSettingsRepository

@Service
class PrivateChannelService {

    @Autowired
    private lateinit var channelRepository: PrivateChannelRepository
    @Autowired
    private lateinit var settingsRepository: PrivateChannelSettingsRepository

    @Transactional
    fun getOrCreateVoiceChannel(userId: Snowflake): Mono<PrivateChannel> {
        return channelRepository.findById(userId.asLong())
            .switchIfEmpty(channelRepository.save(PrivateChannel(userId.asLong())))
    }

    @Transactional
    fun getVoiceChannelByChannelId(channelId: Snowflake): Mono<PrivateChannel> {
        return channelRepository.findByChannelId(channelId.asLong())
    }

    @Transactional
    fun updateVoiceChannel(channel: PrivateChannel): Mono<PrivateChannel> {
        return channelRepository.save(channel)
    }

    @Transactional
    fun updateChannelId(userId: Snowflake, channelId: Snowflake): Mono<PrivateChannel> {
        return channelRepository.findById(userId.asLong())
            .flatMap {
                it.channelId = channelId.asLong()
                return@flatMap channelRepository.save(it)
            }
    }

    @Transactional
    fun setPermanent(userId: Snowflake, permanent: Boolean): Mono<PrivateChannel> {
        return channelRepository.findById(userId.asLong())
            .flatMap {
                it.permanent = permanent
                return@flatMap channelRepository.save(it)
            }
    }

    @Transactional
    fun updateExistence(userId: Snowflake, exist: Boolean): Mono<PrivateChannel> {
        return channelRepository.findById(userId.asLong())
            .flatMap {
                it.exist = exist
                return@flatMap channelRepository.save(it)
            }
    }

    @Transactional
    fun getOrCreateSettings(guildId: Snowflake): Mono<PrivateChannelSettings> {
        return settingsRepository.findById(guildId.asLong())
            .switchIfEmpty(settingsRepository.save(PrivateChannelSettings(guildId.asLong())))
    }

    fun doesChannelExist(channelId: Snowflake): Mono<Boolean> {
        return channelRepository.existsPrivateChannelByChannelId(channelId.asLong())
    }
}