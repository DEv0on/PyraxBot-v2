package com.dev0on.common.modules.automation.channels

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.MessageCreateFields
import discord4j.core.spec.MessageCreateSpec
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import com.dev0on.common.event.discord.DiscordEvent
import com.dev0on.common.modules.automation.videoapi.VideoService
import dev.arbjerg.lavalink.client.Link
import discord4j.rest.util.AllowedMentions
import java.io.ByteArrayInputStream
import java.net.URI
import java.util.regex.Pattern

class IgTtConvertingChannel(channelId: Long) : ChannelSetting(channelId, 1) {

    override fun execute(event: DiscordEvent<MessageCreateEvent>): Mono<Void> {
        if (event.getCancelled()) return Mono.empty()

        val msg = event.event.message.content

        event.setCancelled(true)

        val matcher = LinkOnlyChannel.URL_REGEX.matcher(msg)
        if (!matcher.find()) return Mono.empty()

        when (matcher.group("domain")) {
            "vm.tiktok.com" -> {
                val id = matcher.group("path").replace("/", "")
                return downloadVideoAndSend(event, Endpoint.TIKTOK_SHORT, id,null)
            }
            "www.tiktok.com" -> {
                val username = matcher.group("path").split("/").first().replace("@", "")
                val id = matcher.group("path").split("/").last()
                return downloadVideoAndSend(event, Endpoint.TIKTOK, id, username)
            }
            "instagram.com", "www.instagram.com" -> {
                val path = matcher.group("path")
                if (!path.startsWith("/reel") && !path.startsWith("/p")) return Mono.empty()

                val id = path.split("/")[2]
                return downloadVideoAndSend(event, Endpoint.REEL, id, null)
            }
            else -> return Mono.empty()
        }
    }

    private fun downloadVideoAndSend(
        event: DiscordEvent<MessageCreateEvent>,
        endpoint: Endpoint,
        id: String,
        username: String?
    ): Mono<Void> {
        val videoApi = when (endpoint) {
            Endpoint.TIKTOK -> VideoService.client.getVideo(username!!, id)
            Endpoint.TIKTOK_SHORT -> VideoService.client.getShortVideo(id)
            Endpoint.REEL -> VideoService.client.getReel(id)
        }
        val author = event.event.message.author.get()
        val content = event.event.message.content
        val mentions = AllowedMentions.builder().allowUser(*event.event.message.userMentions.map {it.id}.toTypedArray()).build()
        return event.event.message.delete()
            .then(videoApi)
            .zipWith(event.event.message.channel)
            .publishOn(Schedulers.boundedElastic())
            .flatMap { tuple ->
                val url = URI(tuple.t1.video_url).toURL()
                url.openStream().use {
                    return@flatMap tuple.t2.createMessage(
                        MessageCreateSpec.builder()
                            .content("""
                                `${author.username}:`
                                
                                ${content.replace(LinkOnlyChannel.URL_REGEX.toRegex(), "")}
                            """.trimIndent())
                            .addFile(
                                MessageCreateFields.File.of("video.mp4", ByteArrayInputStream(it.readAllBytes()))
                            )
                            .allowedMentions(mentions)
                            .build()
                    )
                }
            }.then()
    }

    enum class Endpoint {
        TIKTOK,
        TIKTOK_SHORT,
        REEL
    }
}