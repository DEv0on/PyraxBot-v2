package space.happyniggersin.common.utils

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.VoiceStateUpdateEvent

fun VoiceStateUpdateEvent.hasJoinedChannel(channelId: Snowflake): Boolean {
    return (this.isJoinEvent || this.isMoveEvent)
            && this.current.channelId.isPresent
            && this.current.channelId.get() == channelId;
}