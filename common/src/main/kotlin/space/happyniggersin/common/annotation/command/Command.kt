package space.happyniggersin.common.annotation.command

import org.springframework.aot.hint.annotation.Reflective
import org.springframework.stereotype.Component

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
@Reflective
annotation class Command(val name: String = "")
