package space.happyniggersin.common.utils

import dev.arbjerg.lavalink.client.player.Track
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.time.temporal.ChronoUnit

fun Track.getName(): String = "${info.author} - ${info.title}"

fun Track.getFormattedLength(): String {
    val duration = java.time.Duration.of(info.length, ChronoUnit.MILLIS)

    return String.format("%d:%02d", duration.seconds / 60, duration.seconds % 60)
}

fun Track.toApiTrack(): dev.arbjerg.lavalink.protocol.v4.Track {
    return dev.arbjerg.lavalink.protocol.v4.Track(
        encoded,
        info,
        Json.parseToJsonElement(pluginInfo.toString()) as JsonObject,
        Json.parseToJsonElement(userData.toString()) as JsonObject
    )
}