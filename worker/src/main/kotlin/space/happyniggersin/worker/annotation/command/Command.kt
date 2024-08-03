package space.happyniggersin.worker.annotation.command

import org.springframework.stereotype.Component

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class Command(val name: String = "")
