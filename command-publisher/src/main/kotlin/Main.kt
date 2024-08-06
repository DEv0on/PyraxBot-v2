package space.happyniggersin

fun main() {
    val bot = DiscordBot(System.getenv("BOT_TOKEN"))

    val applicationId = bot.client.restClient.applicationId.block()

    val commands = bot.getCommands()

    bot.client.restClient.applicationService
        .bulkOverwriteGuildApplicationCommand(
            applicationId!!,
            878189870873460736L,
            commands
        ).blockLast()
}