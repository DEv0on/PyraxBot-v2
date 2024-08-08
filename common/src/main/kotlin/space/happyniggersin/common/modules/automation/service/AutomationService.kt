package space.happyniggersin.common.modules.automation.service

import discord4j.common.util.Snowflake
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.where
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import space.happyniggersin.common.modules.automation.entity.AutomationSettings
import space.happyniggersin.common.modules.automation.repository.AutomationRepository
import space.happyniggersin.common.modules.automation.types.ChannelSetting

@Service
class AutomationService {
    @Autowired
    private lateinit var automationService: AutomationRepository
    @Autowired
    private lateinit var template: ReactiveMongoTemplate

    @Transactional
    fun getOrCreate(id: Long): Mono<AutomationSettings> {
        return automationService.findById(id)
            .switchIfEmpty(automationService.save(AutomationSettings(id)))
    }

    @Transactional
    fun save(automationSettings: AutomationSettings): Mono<AutomationSettings> {
        return automationService.save(automationSettings)
    }

    @Transactional
    fun addChannelSetting(guildId: Snowflake, channelSetting: ChannelSetting): Mono<Void> {
        return template.update(AutomationSettings::class.java)
            .matching(where(AutomationSettings::guildId).`is`(guildId.asLong()))
            .apply(
                Update().push("channelSettings", channelSetting)
            )
            .first()
            .then()

    }
}