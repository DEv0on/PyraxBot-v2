package com.dev0on.common.data

import org.springframework.data.annotation.Id

abstract class AbstractGuildSettings() {

    @Id
    var guildId: Long = 0L
}