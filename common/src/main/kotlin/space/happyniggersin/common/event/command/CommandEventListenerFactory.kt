package space.happyniggersin.common.event.command

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.command.ApplicationCommandOption
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import space.happyniggersin.common.annotation.command.Option
import java.lang.reflect.Method
import java.util.*
import java.util.stream.IntStream

@Component
class CommandEventListenerFactory {

    @Autowired
    private lateinit var ctx: ApplicationContext

    @Autowired
    private lateinit var commandProcessor: CommandBeanPostProcess

    fun invoke(event: ChatInputInteractionEvent): Mono<Void> {
        val commandName = getCommandName(event)
        val method = commandProcessor.findMethod(commandName.joinToString(".")) ?: return Mono.empty()
        val bean = ctx.getBean(method.declaringClass) ?: return Mono.empty()
        val nodes = commandName.sliceArray(1 until commandName.size).toMutableList()
        val options = if (nodes.size > 0) getOptions(event.options, nodes) else event.options

        val resolvedOptions = resolveOptions(method, bean, options).toTypedArray()

        return method.invoke(bean, event, *resolvedOptions) as Mono<Void>
    }

    fun resolveOptions(method: Method, bean: Any, options: List<ApplicationCommandInteractionOption>): List<Any> {
        return method.parameters
            .toList()
            .toTypedArray()
            .slice(1 until Math.min(method.parameters.size, options.size + 1))
            .map { field ->
                val optionName = AnnotatedElementUtils.findMergedAnnotation(field, Option::class.java)!!.name
                val option = options.find { it.name == optionName } ?: return@map Optional.empty<Any>()
                return@map getCastedOptional(option)
            }
    }

    fun getCastedOptional(option: ApplicationCommandInteractionOption): Optional<Any> {
        return when (option.type) {
            ApplicationCommandOption.Type.STRING -> option.value.map(ApplicationCommandInteractionOptionValue::asString)
            ApplicationCommandOption.Type.INTEGER -> option.value.map(ApplicationCommandInteractionOptionValue::asLong)
            ApplicationCommandOption.Type.BOOLEAN -> option.value.map(ApplicationCommandInteractionOptionValue::asBoolean)
            ApplicationCommandOption.Type.USER -> option.value.map(ApplicationCommandInteractionOptionValue::asUser)
            ApplicationCommandOption.Type.CHANNEL -> option.value.map(ApplicationCommandInteractionOptionValue::asChannel)
            ApplicationCommandOption.Type.ROLE -> option.value.map(ApplicationCommandInteractionOptionValue::asRole)
            ApplicationCommandOption.Type.NUMBER -> option.value.map(ApplicationCommandInteractionOptionValue::asDouble)
            ApplicationCommandOption.Type.ATTACHMENT -> option.value.map(ApplicationCommandInteractionOptionValue::asAttachment)
            else -> throw IllegalArgumentException("Unknown option type ${option.type}")
        }
    }

    fun getOptions(
        options: List<ApplicationCommandInteractionOption>,
        nodes: MutableList<String>
    ): List<ApplicationCommandInteractionOption> {
        var option = options.firstOrNull() ?: return emptyList()
        nodes.removeFirst()

        IntStream.range(0, nodes.size).forEach {
            option = option.options.first { nodes.removeFirst() == it.name }
        }
        return option.options
    }

    fun getCommandName(event: ChatInputInteractionEvent): Array<String> {
        val nodes = mutableListOf(event.commandName)
        getCommandName(event.options, nodes)
        return nodes.toTypedArray()
    }

    fun getCommandName(options: List<ApplicationCommandInteractionOption>, nodes: MutableList<String>) {
        val subCommand =
            options.firstOrNull { it.type == ApplicationCommandOption.Type.SUB_COMMAND || it.type == ApplicationCommandOption.Type.SUB_COMMAND_GROUP }
        if (subCommand != null) {
            nodes.add(subCommand.name)
            if (subCommand.type == ApplicationCommandOption.Type.SUB_COMMAND_GROUP) {
                getCommandName(subCommand.options, nodes)
            }
        }
    }
}