package com.dev0on.common.modules.music

import discord4j.common.util.Snowflake
import discord4j.voice.VoiceConnection
import discord4j.voice.VoiceGatewayEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class VoidVoiceConnection: VoiceConnection {
    override fun events(): Flux<VoiceGatewayEvent> = Flux.empty()

    override fun stateEvents(): Flux<VoiceConnection.State> = Flux.empty()

    override fun disconnect(): Mono<Void> = Mono.empty()

    override fun getGuildId(): Snowflake = Snowflake.of(0)

    override fun getChannelId(): Mono<Snowflake> = Mono.just(Snowflake.of(0))

    override fun reconnect(): Mono<Void> = Mono.empty()
}