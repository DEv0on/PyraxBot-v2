package space.happyniggersin.common.annotation.event

import discord4j.core.event.domain.Event
import kotlin.reflect.KClass

annotation class DiscordEventListener(val type: KClass<out Event>, val order: Int, val cancellable: Boolean = false)
