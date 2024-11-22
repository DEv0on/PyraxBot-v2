package com.dev0on.common.modules.music

import dev.arbjerg.lavalink.client.player.*
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionReplyEditSpec
import reactor.core.publisher.Mono
import com.dev0on.common.utils.getName
import com.dev0on.common.utils.toApiTrack

class AudioLoader(val event: ChatInputInteractionEvent, val manager: GuildAudioManager) :
    java.util.function.Function<LavalinkLoadResult, Mono<Void>> {
    private fun loadFailed(result: LoadFailed): Mono<Void> {
        return event.editReply(
            InteractionReplyEditSpec.create()
                .withEmbeds(
                    EmbedCreateSpec.create()
                        .withDescription("Błąd podczas ładowania utworu! `${result.exception.message}`")
                )
        )
            .then()
    }

    private fun noMatches(): Mono<Void> {
        return event.editReply(
            InteractionReplyEditSpec.create()
                .withEmbeds(
                    EmbedCreateSpec.create()
                        .withDescription("Nie znaleziono utworu")
                )
        )
            .then()
    }

    private fun onPlaylistLoaded(result: PlaylistLoaded): Mono<Void> {
        return this.manager.scheduler.enqueuePlaylist(result.tracks)
            .then(
                event.editReply(
                    InteractionReplyEditSpec.create()
                        .withEmbeds(
                            EmbedCreateSpec.create()
                                .withDescription("Dodano `${result.tracks.size}` utworów do kolejki z playlisty `${result.info.name}`\n\nDodał: ${event.interaction.user.mention}")
                        )
                )
            )
            .then()
    }

    private fun onSearchResultLoaded(result: SearchResult): Mono<Void> {
        return onTrackLoaded(TrackLoaded(LoadResult.TrackLoaded(result.tracks[0].toApiTrack())))
    }

    private fun onTrackLoaded(result: TrackLoaded): Mono<Void> {
        val track = result.track

        val userId = event.interaction.user.id
        val first = event.getOption("first")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .orElse(false)

        return this.manager.scheduler.enqueue(track, first)
            .then(
                event.editReply(
                    InteractionReplyEditSpec.create()
                        .withEmbeds(
                            EmbedCreateSpec.create()
                                .withDescription("Dodano do kolejki `${track.getName()}`\n\nDodał: ${event.interaction.user.mention}")
                        )
                )
            )
            .then()
    }

    override fun apply(loadResult: LavalinkLoadResult): Mono<Void> {
        when (loadResult) {
            is TrackLoaded -> {
                return this.onTrackLoaded(loadResult)
            }

            is PlaylistLoaded -> {
                return this.onPlaylistLoaded(loadResult)
            }

            is SearchResult -> {
                return this.onSearchResultLoaded(loadResult)
            }

            is NoMatches -> {
                return this.noMatches()
            }

            is LoadFailed -> {
                return this.loadFailed(loadResult)
            }

            else -> {
                return Mono.empty()
            }
        }
    }

}