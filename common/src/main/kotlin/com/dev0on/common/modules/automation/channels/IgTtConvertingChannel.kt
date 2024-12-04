package com.dev0on.common.modules.automation.channels

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.MessageCreateFields
import discord4j.core.spec.MessageCreateSpec
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import com.dev0on.common.event.discord.DiscordEvent
import com.dev0on.common.modules.automation.videoapi.VideoService
import java.io.ByteArrayInputStream
import java.net.URI

class IgTtConvertingChannel(channelId: Long): ChannelSetting(channelId, 1) {
    override fun execute(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.getCancelled()) return Mono.empty()

        val msg = event.event.message.content

        event.setCancelled(true)
        return event.event.message.delete().then(Mono.defer {
            val msgSpl = msg.split("/")
            if (msg.contains("vm.tiktok.com")) {
                if (msgSpl.size < 4)
                    return@defer Mono.empty()
                return@defer downloadVideoAndSend(event, Endpoint.TIKTOK_SHORT, msgSpl[3], null)
            } else if(msg.contains("www.tiktok.com")) {
                if (msgSpl.size < 6)
                    return@defer Mono.empty()

                return@defer downloadVideoAndSend(event, Endpoint.TIKTOK, msgSpl[5], msgSpl[3].replace("@", ""))
            } else if(msg.contains("instagram.com/reel") || msg.contains("instagram.com/p")) {
                if (msgSpl.size < 5)
                    return@defer Mono.empty()

                return@defer downloadVideoAndSend(event, Endpoint.REEL, msgSpl[4], null)
            }
            return@defer Mono.empty()
        })
    }

    private fun downloadVideoAndSend(event: DiscordEvent<MessageCreateEvent>, endpoint: Endpoint, id: String, username: String?): Mono<Void> {
        val videoApi = when(endpoint) {
            Endpoint.TIKTOK ->  VideoService.client.getVideo(username!!, id)
            Endpoint.TIKTOK_SHORT -> VideoService.client.getShortVideo(id)
            Endpoint.REEL -> VideoService.client.getReel(id)
        }

        return videoApi
            .zipWith(event.event.message.channel)
            .publishOn(Schedulers.boundedElastic())
            .flatMap { tuple ->
                val url = URI(tuple.t1.video_url).toURL()
                url.openStream().use {
                    return@flatMap tuple.t2.createMessage(MessageCreateSpec.builder()
                        .addFile(
                            MessageCreateFields.File.of("video.mp4", ByteArrayInputStream(it.readAllBytes()))
                        )
                        .build())
                }
            }.then()
    }

    enum class Endpoint {
        TIKTOK,
        TIKTOK_SHORT,
        REEL
    }
}