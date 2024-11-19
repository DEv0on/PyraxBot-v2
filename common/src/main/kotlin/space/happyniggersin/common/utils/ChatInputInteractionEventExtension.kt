package space.happyniggersin.common.utils

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import space.happyniggersin.common.data.AbstractGuildSettings
import space.happyniggersin.common.data.GuildLang
import space.happyniggersin.common.exception.CommandInvokeException

//fun <T : AbstractGuildSettings> ChatInputInteractionEvent.checkRequiredRoles(
//    settings: T,
//    lang: GuildLang,
//    perms: Set<Long>
//): Mono<Tuple2<T, GuildLang>> {
//    if (perms.isEmpty())
//        return Mono.error(CommandInvokeException(this, lang.commandNoPermissions))
//
//    return this.interaction.member.get().roles
//        .filter { perms.contains(it.id.asLong()) }
//        .collectList()
//        .flatMap {
//            if (it.size > 0)
//                return@flatMap Mono.just(Tuples.of(settings, lang))
//            return@flatMap Mono.error(CommandInvokeException(this, lang.commandNoPermissions))
//        }
//}

fun <T : AbstractGuildSettings> ChatInputInteractionEvent.checkRequiredRoles(
    settings: T,
    perms: Set<Long>
): Mono<T> {
    if (perms.isEmpty())
        return Mono.error(CommandInvokeException(this, "Nie posiadasz uprawnień do wykonania tej komendy."))

    return this.interaction.member.get().roles
        .filter { perms.contains(it.id.asLong()) }
        .collectList()
        .flatMap {
            if (it.size > 0)
                return@flatMap Mono.just(settings)
            return@flatMap Mono.error(CommandInvokeException(this, "Nie posiadasz uprawnień do wykonania tej komendy."))
        }
}