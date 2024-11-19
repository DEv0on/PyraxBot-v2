package space.happyniggersin.common.modules.music

import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.player.LavalinkPlayer
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.channel.AudioChannel
import discord4j.core.`object`.entity.channel.VoiceChannel
import reactor.core.publisher.Mono

data class GuildAudioManager(
    val guildId: Snowflake,
    val gateway: GatewayDiscordClient,
    val lavalinkClient: LavalinkClient,
) {
    val scheduler: TrackScheduler = TrackScheduler(gateway, this)
    var channelId: Snowflake? = null

    fun connect(channel: AudioChannel): Mono<Void> {
        this.channelId = channel.id
        return channel.sendConnectVoiceState(false, true)
    }

    fun disconnect(gateway: GatewayDiscordClient): Mono<Void> {
        val channel = channelId ?: return Mono.empty()

        return gateway
            .getChannelById(channel)
            .ofType(VoiceChannel::class.java)
            .flatMap(VoiceChannel::sendDisconnectVoiceState)
            .then(Mono.fromCallable { this.channelId = null })
            .then(this.stop())
    }

    fun stop(): Mono<Void> {
        this.scheduler.queue.clear()

        return this.getPlayer()
            .flatMap { player ->
                player
                    .setPaused(true)
                    .setTrack(null)
            }
            .then()
    }

    fun skip(): Mono<Void> {
        val track = this.scheduler.queue.poll()

        return this.getPlayer()
            .flatMap { player ->
                player
                    .setPaused(false)
                    .setTrack(track)
            }
            .then()
    }

    fun setVolume(volume: Int): Mono<Void> {
        val cappedVol = 0.coerceAtLeast(1000.coerceAtMost(volume))

        return this.getLink()
            .flatMap { link ->
                link.createOrUpdatePlayer()
                    .setVolume(cappedVol)
            }
            .then()
    }

    fun getLink(): Mono<Link> {
        return Mono.justOrEmpty(this.lavalinkClient.getLinkIfCached(this.guildId.asLong()))
    }

    fun getPlayer(): Mono<LavalinkPlayer> {
        return this.getLink().mapNotNull(Link::cachedPlayer)
    }
}