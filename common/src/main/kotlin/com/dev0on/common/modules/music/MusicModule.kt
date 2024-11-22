package com.dev0on.common.modules.music

import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.NodeOptions
import dev.arbjerg.lavalink.client.event.TrackEndEvent
import dev.arbjerg.lavalink.client.event.TrackStartEvent
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import com.dev0on.common.utils.getUserIdFromToken
import dev.arbjerg.lavalink.libraries.discord4j.installVoiceHandler

@Component
class MusicModule {

    @Autowired
    lateinit var gateway: GatewayDiscordClient
    lateinit var lavalinkClient: LavalinkClient

    val audioManagers: MutableMap<Snowflake, GuildAudioManager> = mutableMapOf()

    @EventListener(ApplicationReadyEvent::class)
    fun loadMusicModule(event: ApplicationReadyEvent) {
        this.lavalinkClient = LavalinkClient(getUserIdFromToken(System.getenv("BOT_TOKEN")))
        this.lavalinkClient.loadBalancer.addPenaltyProvider(VoiceRegionPenaltyProvider())

        registerListeners()
        registerNodes()
        this.gateway.installVoiceHandler(this.lavalinkClient)
    }

    private fun registerListeners() {
        lavalinkClient.on(TrackStartEvent::class.java)
            .flatMap { event ->
                val manager = this.audioManagers[Snowflake.of(event.guildId)] ?: return@flatMap Mono.empty()

                return@flatMap manager.scheduler.onTrackStart(event.track)
            }
            .subscribe()

        lavalinkClient.on(TrackEndEvent::class.java)
            .flatMap { event ->
                val manager = this.audioManagers[Snowflake.of(event.guildId)] ?: return@flatMap Mono.empty()

                return@flatMap manager.scheduler.onTrackEnd(event.track, event.endReason)
            }
            .subscribe()
    }

    private fun registerNodes() {
        val nodeName = System.getenv("LAVALINK_NODE_NAME")
        val nodeUri = System.getenv("LAVALINK_NODE_URI")
        val nodePass = System.getenv("LAVALINK_NODE_PASSWORD")

        this.lavalinkClient.addNode(NodeOptions.Builder()
            .setName(nodeName)
            .setServerUri(nodeUri)
            .setPassword(nodePass)
            .build())
    }

    fun getOrCreateAudioManager(guildId: Snowflake): GuildAudioManager {
        synchronized(this) {
            var manager = this.audioManagers[guildId]
            if (manager == null) {
                manager = GuildAudioManager(guildId, this.gateway, this.lavalinkClient)
                this.audioManagers[guildId] = manager
            }
            return manager
        }
    }
}