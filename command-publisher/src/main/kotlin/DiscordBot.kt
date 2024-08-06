package space.happyniggersin

import discord4j.common.JacksonResources
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.discordjson.json.ApplicationCommandRequest
import java.io.File

class DiscordBot(token: String) {
    var client: GatewayDiscordClient = DiscordClient.create(token)
        .login()
        .block()!!

    fun getCommands(): List<ApplicationCommandRequest> {
        val jackson = JacksonResources.create()
        val commands = mutableListOf<ApplicationCommandRequest>()
        File(DiscordBot::class.java.classLoader.getResource("commands/")?.file ?: return emptyList()).listFiles()
            ?.forEach {
                commands.add(jackson.objectMapper.readValue(it.readText(), ApplicationCommandRequest::class.java))
            }

        return commands
    }
}