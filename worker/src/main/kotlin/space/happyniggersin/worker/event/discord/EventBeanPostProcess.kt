package space.happyniggersin.worker.event.discord

import discord4j.core.event.domain.Event
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.MethodIntrospector
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Component
import space.happyniggersin.worker.annotation.event.DiscordEventListener
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Component
class EventBeanPostProcess : BeanPostProcessor {

    val registeredEvents: MutableMap<KClass<out Event>, MutableList<ListenerDefinition>> = mutableMapOf()

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        MethodIntrospector.selectMethods(bean.javaClass, MethodIntrospector.MetadataLookup {
            AnnotatedElementUtils.findMergedAnnotation(it, DiscordEventListener::class.java)
        })
            .filter {
                it.key.parameters.size == 1
                        && DiscordEvent::class.java.isAssignableFrom(it.key.parameterTypes[0])
                        && it.value.type.isSubclassOf(Event::class)
            }
            .forEach {
                val method = it.key
                val anno = it.value
                registeredEvents.putIfAbsent(anno.type, mutableListOf())

                val listeners = registeredEvents[anno.type]
                listeners!!.add(ListenerDefinition(method, bean, anno.order, anno.cancellable))
            }


        return super.postProcessAfterInitialization(bean, beanName)
    }
}