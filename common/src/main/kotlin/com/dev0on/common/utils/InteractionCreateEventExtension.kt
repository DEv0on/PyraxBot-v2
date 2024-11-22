package com.dev0on.common.utils

import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec
import discord4j.core.spec.InteractionReplyEditSpec
import discord4j.rest.util.Color
import reactor.core.publisher.Mono

fun DeferrableInteractionEvent.embedReply(content: String, color: Color = Color.RED, ephemeral: Boolean = false): Mono<Void> {
    return this.reply.flatMap {
        if (it == null) {
            return@flatMap this.reply(
                InteractionApplicationCommandCallbackSpec.builder()
                    .ephemeral(ephemeral)
                    .embeds(
                        EmbedCreateSpec.builder()
                            .color(color)
                            .description(content)
                            .build()
                    )
                    .build()

            )
        }
        return@flatMap editEmbedReply(content)
    }
}

private fun DeferrableInteractionEvent.editEmbedReply(content: String, color: Color = Color.RED): Mono<Void> {
    return this.editReply(
        InteractionReplyEditSpec.create()
            .withEmbeds(
                EmbedCreateSpec.builder()
                    .color(color)
                    .description(content)
                    .build()
            )
    ).then()
}