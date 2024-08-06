package space.happyniggersin.worker.annotation.command

import org.springframework.aot.hint.annotation.Reflective

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Reflective
annotation class Option(val name: String)
