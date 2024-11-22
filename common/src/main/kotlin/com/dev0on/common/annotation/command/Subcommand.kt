package com.dev0on.common.annotation.command

import org.springframework.aot.hint.annotation.Reflective
import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
@Reflective
annotation class Subcommand(val name: String = "")