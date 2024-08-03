package space.happyniggersin.worker.event.command

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.MethodIntrospector
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import space.happyniggersin.worker.annotation.command.Command
import space.happyniggersin.worker.annotation.command.Subcommand
import java.lang.reflect.Method
import java.util.*

@Component
class CommandBeanPostProcess : BeanPostProcessor {

    val registeredMethods = HashMap<String, Method>()

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (AnnotatedElementUtils.hasAnnotation(bean.javaClass, Subcommand::class.java))
            return super.postProcessAfterInitialization(bean, beanName)
        if (!AnnotatedElementUtils.hasAnnotation(bean.javaClass, Command::class.java))
            return super.postProcessAfterInitialization(bean, beanName)

        val methods = findMethods(bean.javaClass)

        methods.forEach { method ->
            register(method.key, method.value)
        }
        return super.postProcessAfterInitialization(bean, beanName)
    }

    fun findMethods(element: Class<*>): Map<String, Method> {
        val root = AnnotatedElementUtils.findMergedAnnotation(element, Command::class.java) ?: return emptyMap()

        return findMethods(element, root.name)
    }

    fun findMethods(element: Class<*>, path: String): Map<String, Method> {
        val methodMap = mutableMapOf<String, Method>()
        if (!AnnotatedElementUtils.hasAnnotation(
                element,
                Subcommand::class.java
            ) && !AnnotatedElementUtils.hasAnnotation(element, Command::class.java)
        ) return methodMap
        val anno = AnnotatedElementUtils.findMergedAnnotation(element, Subcommand::class.java)?.name
            ?: AnnotationUtils.findAnnotation(element, Command::class.java)!!.name
        val newPath = (if (anno.isEmpty()) {
            path
        } else {
            "${path}.${anno}"
        })
        val methods = MethodIntrospector.selectMethods(element, MethodIntrospector.MetadataLookup {
            AnnotatedElementUtils.findMergedAnnotation(it, Command::class.java)
        })
        methods.forEach { method ->
            methodMap[newPath + (if (newPath.isEmpty()) {
                ""
            } else {
                "."
            }) + method.value.name] = method.key
        }
        element.classes.forEach { clazz ->
            methodMap.putAll(
                findMethods(
                    clazz, (if (path.equals(newPath)) {
                        "${path}.${anno}"
                    } else {
                        path
                    })
                )
            )
        }
        return methodMap
    }

    fun supportsMethod(method: Method): Boolean {
        return AnnotatedElementUtils.hasAnnotation(method, Command::class.java) &&
                Mono::class.java.isAssignableFrom(method.returnType) &&
                method.parameterTypes.isNotEmpty() &&
                ChatInputInteractionEvent::class.java.isAssignableFrom(method.parameterTypes[0])
    }

    fun register(commandName: String, method: Method) {
        if (!supportsMethod(method))
            throw IllegalArgumentException("Method " + method.name + " is not supported as command")

        registeredMethods[commandName] = method
    }

    fun findMethod(commandName: String): Method? {
        return registeredMethods[commandName]
    }
}