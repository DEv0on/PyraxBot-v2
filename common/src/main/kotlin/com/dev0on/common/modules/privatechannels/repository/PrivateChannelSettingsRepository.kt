package com.dev0on.common.modules.privatechannels.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import com.dev0on.common.modules.privatechannels.entity.PrivateChannelSettings

@Component
interface PrivateChannelSettingsRepository: ReactiveCrudRepository<PrivateChannelSettings, Long>