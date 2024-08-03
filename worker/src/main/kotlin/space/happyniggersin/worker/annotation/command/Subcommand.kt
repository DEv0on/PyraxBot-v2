package space.happyniggersin.worker.annotation.command

import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class Subcommand(val name: String = "")