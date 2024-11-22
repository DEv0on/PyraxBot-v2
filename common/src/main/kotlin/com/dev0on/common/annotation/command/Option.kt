package com.dev0on.common.annotation.command

import org.springframework.aot.hint.annotation.Reflective

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Reflective
annotation class Option(val name: String)
