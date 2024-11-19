package space.happyniggersin.common.modules.music

import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.protocol.v4.Message
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.VoiceState
import discord4j.core.`object`.entity.PartialMember
import discord4j.core.`object`.entity.channel.TextChannel
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import space.happyniggersin.common.utils.getFormattedLength
import space.happyniggersin.common.utils.getName
import java.time.Instant
import java.util.*

data class TrackScheduler(
    val gateway: GatewayDiscordClient,
    val manager: GuildAudioManager,
) {
    val queue: Deque<Track> = LinkedList()
    var loop = false
    var lastUpdate: Instant = Instant.now()

    fun enqueue(track: Track, first: Boolean): Mono<Void> {
        return this.manager.getPlayer()
            .flatMap {
                if (it.track == null)
                    return@flatMap this.startTrack(track)

                if (first)
                    this.queue.offerFirst(track)
                else
                    this.queue.offer(track)
                return@flatMap Mono.empty()
            }
            .switchIfEmpty { this.startTrack(track) }
    }

    fun enqueuePlaylist(playlist: List<Track>): Mono<Void> {
        this.queue.addAll(playlist)

        return this.manager.getPlayer()
            .flatMap {
                if (it.track != null)
                    return@flatMap Mono.empty()

                return@flatMap this.startTrack(this.queue.poll())
            }
            .switchIfEmpty { this.startTrack(this.queue.poll()) }
    }

    fun onTrackStart(track: Track): Mono<Void> {
        return gateway.getSelfMember(manager.guildId)
            .flatMap(PartialMember::getVoiceState)
            .flatMap(VoiceState::getChannel)
            .ofType(TextChannel::class.java)
            .flatMap { it.createMessage("Playing `${track.getName()}`!\nDuration: `${track.getFormattedLength()}`") }
            .then()
    }

    fun onTrackEnd(lastTrack: Track, endReason: Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason): Mono<Void> {
        this.lastUpdate = Instant.now()

        if (!endReason.mayStartNext)
            return Mono.empty()

        val nextTrack = this.queue.poll() ?: return Mono.empty()

        if (this.loop) {
            this.queue.offer(nextTrack)
            return Mono.empty()
        }

        return this.startTrack(nextTrack)
    }

    fun startTrack(track: Track): Mono<Void> {
        return this.manager.getLink()
            .flatMap {
                it.createOrUpdatePlayer()
                    .setTrack(track)
                    .setVolume(100)

            }.then()
    }
}