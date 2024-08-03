package space.happyniggersin.worker.annotation.command

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Option(val name: String)
