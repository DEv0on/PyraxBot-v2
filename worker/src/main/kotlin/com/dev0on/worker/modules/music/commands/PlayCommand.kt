package com.dev0on.worker.modules.music.commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.VoiceState
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono
import com.dev0on.common.annotation.command.Command
import com.dev0on.common.annotation.command.Option
import com.dev0on.common.exception.CommandInvokeException
import com.dev0on.common.modules.music.AudioLoader
import com.dev0on.common.modules.music.VoidVoiceConnection
import com.dev0on.common.modules.music.MusicModule
import java.util.*

@Command
class PlayCommand {

    @Autowired
    private lateinit var musicModule: MusicModule

    @Command("play")
    fun onPlay(event: ChatInputInteractionEvent, @Option("track") track: Optional<String>): Mono<Void> {
        return event.deferReply()
            .then(event.interaction.guild)
            .flatMap { it.voiceConnection }
            .defaultIfEmpty(VoidVoiceConnection())
            .flatMap { joinChannel(event) }
            .then(processTrack(event, track))
    }

    fun joinChannel(event: ChatInputInteractionEvent): Mono<Void> {
        val manager = musicModule.getOrCreateAudioManager(event.interaction.guildId.get())

        return event.interaction.member
            .map {
                it.voiceState
                    .flatMap(VoiceState::getChannel)
                    .flatMap(manager::connect)
            }
            .orElseGet { Mono.empty() }
    }

    fun processTrack(event: ChatInputInteractionEvent, track: Optional<String>): Mono<Void> {
        if (event.interaction.guildId.isEmpty) return Mono.error(CommandInvokeException(event, "Guild is null"))

        val link = musicModule.lavalinkClient.getOrCreateLink(event.interaction.guildId.get().asLong())
        val manager = musicModule.getOrCreateAudioManager(event.interaction.guildId.get())

        return link
            .loadItem(track.get())
            .flatMap(AudioLoader(event, manager))
    }
}