package michi.bot.util

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.Michi
import michi.bot.commands.CommandScope
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("GuildCommandUpdater")

suspend fun updateGuildCommands(guild: Guild) {

    val commandData: MutableList<CommandData> = ArrayList()

    val cmdInfo: YamlMap = getYML(guild).yamlMap["command_info"]!!
    val description: YamlMap = cmdInfo.yamlMap["description"]!!
    val argsDescription: YamlMap = cmdInfo.yamlMap["arguments_description"]!!

    Michi.commandList.forEach { cmd ->

        if (cmd.scope != CommandScope.GUILD_SCOPE) return@forEach

        if (description.getText(cmd.name.lowercase()) == "404 - NOT FOUND")
            logger.warn("Couldn't find the description for the command ${cmd.name}.")

        val command = Commands.slash(cmd.name.lowercase(), description.getText(cmd.name.lowercase()))
        for (arg in cmd.arguments) {

            val argDesc: YamlMap = argsDescription[cmd.name.lowercase()]!!

            if (argDesc.getText(arg.name.lowercase()) == "404 - NOT FOUND")
                logger.warn("Couldn't find the description for the argument ${arg.name} of the command ${cmd.name}")

            command.addOption(
                arg.type,
                arg.name.lowercase(),
                argDesc.getText(arg.name.lowercase()),
                arg.isRequired,
                arg.hasAutoCompletion
            )
        }
        commandData += command
    }
    guild.updateCommands().addCommands(commandData).queue()

}

fun updateCommands(event: ReadyEvent) {
    val commandData: MutableList<CommandData> = ArrayList()

    val cmdInfo: YamlMap = getYML(Language.EN_US).yamlMap["command_info"]!!
    val description: YamlMap = cmdInfo.yamlMap["description"]!!
    val argsDescription: YamlMap = cmdInfo.yamlMap["arguments_description"]!!

    Michi.commandList.forEach { cmd ->

        if (description.getText(cmd.name.lowercase()) == "404 - NOT FOUND")
            logger.warn("Couldn't find the description for the command ${cmd.name}.")

        if (cmd.scope != CommandScope.GLOBAL_SCOPE) return@forEach

        val command = Commands.slash(cmd.name.lowercase(), description.getText(cmd.name.lowercase()))
        for (arg in cmd.arguments) {

            val argDesc: YamlMap = argsDescription[cmd.name.lowercase()]!!
            if (argDesc.getText(arg.name.lowercase()) == "404 - NOT FOUND")
                logger.warn("Description for the argument ${arg.name} of the command ${cmd.name} wasn't found.")

            command.addOption(
                arg.type,
                arg.name.lowercase(),
                argDesc.getText(arg.name.lowercase()),
                arg.isRequired,
                arg.hasAutoCompletion
            )

        }
        commandData += command
    }

    event.jda.updateCommands().addCommands(commandData).queue()
}