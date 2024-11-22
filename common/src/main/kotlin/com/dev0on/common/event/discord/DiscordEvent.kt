package com.dev0on.common.event.discord

class DiscordEvent<T>(val event: T, private val cancellable: Boolean = false) {
    private var cancelled: Boolean = false

    fun setCancelled(cancelled: Boolean) {
        if (!cancellable) throw IllegalStateException("Event is not cancellable")
        this.cancelled = cancelled
    }

    fun getCancelled() = cancelled
}