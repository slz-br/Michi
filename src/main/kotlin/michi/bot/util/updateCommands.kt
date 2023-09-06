package michi.bot.util

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import michi.bot.Michi
import michi.bot.commands.CommandScope
import michi.bot.database.dao.UserDAO
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

suspend fun updateGuildCommands(guild: Guild) {

    val commandData: MutableList<CommandData> = ArrayList()

    val cmdInfo: YamlMap = getYML(guild).yamlMap["command_info"]!!
    val argsDescription: YamlMap = cmdInfo.yamlMap["arguments_description"]!!

    Mutex().withLock {
        val randomMember = guild.members[0].user
        if (!randomMember.isBot) UserDAO.post(randomMember)

        val mapOfRegions = mapOf(
            Language.PT_BR to DiscordLocale.PORTUGUESE_BRAZILIAN,
            Language.EN_US to DiscordLocale.ENGLISH_US,
            Language.EN_US to DiscordLocale.ENGLISH_UK
        )

        Michi.commandList.forEach { cmd ->

            if (cmd.scope != CommandScope.GUILD_SCOPE) return@forEach

            val command = Commands.slash(cmd.name.lowercase(), cmd.descriptionLocalization[DiscordLocale.ENGLISH_US]!!)

            command.setDescriptionLocalizations(cmd.descriptionLocalization)

            for (arg in cmd.arguments) {
                val argDesc: YamlMap = argsDescription[cmd.name.lowercase()]!!

                command.addOption(
                    arg.type,
                    arg.name.lowercase(),
                    argDesc.getText(arg.name.lowercase()),
                    arg.isRequired,
                    arg.hasAutoCompletion
                )
            }

            command.options.forEach {
                val argDescLocalizations = mapOfRegions.map { locale ->
                    val cmdInfoLocalization: YamlMap = getYML(locale.key).yamlMap["command_info"]!!
                    val cmdDescriptionLocalization: YamlMap = cmdInfoLocalization["arguments_description"]!!
                    val argDescLocalization: YamlMap = cmdDescriptionLocalization[cmd.name.lowercase()]!!
                    locale.value to argDescLocalization.getText(it.name.lowercase())
                }.toMap()

                it.setDescriptionLocalizations(argDescLocalizations)
            }

            commandData += command
        }
        guild.updateCommands().addCommands(commandData).queue()
    }
}

fun updateCommands(event: ReadyEvent) {
    val commandData: MutableList<CommandData> = ArrayList()

    val cmdInfo: YamlMap = getYML(Language.EN_US).yamlMap["command_info"]!!
    val argsDescription: YamlMap = cmdInfo.yamlMap["arguments_description"]!!

    val mapOfRegions = mapOf(
        Language.PT_BR to DiscordLocale.PORTUGUESE_BRAZILIAN,
        Language.EN_US to DiscordLocale.ENGLISH_US,
        Language.EN_US to DiscordLocale.ENGLISH_UK
    )

    Michi.commandList.forEach { cmd ->

        if (cmd.scope != CommandScope.GLOBAL_SCOPE) return@forEach

        val command = Commands.slash(cmd.name.lowercase(), cmd.descriptionLocalization[DiscordLocale.ENGLISH_US]!!)

        command.setDescriptionLocalizations(cmd.descriptionLocalization)

        for (arg in cmd.arguments) {

            val argDesc: YamlMap = argsDescription[cmd.name.lowercase()]!!

            command.addOption(
                arg.type,
                arg.name.lowercase(),
                argDesc.getText(arg.name.lowercase()),
                arg.isRequired,
                arg.hasAutoCompletion
            )

        }

        command.options.forEach {
            val argDescLocalizations = mapOfRegions.map { locale ->
                val cmdInfoLocalization: YamlMap = getYML(locale.key).yamlMap["command_info"]!!
                val cmdDescriptionLocalization: YamlMap = cmdInfoLocalization["arguments_description"]!!
                val argDescLocalization: YamlMap = cmdDescriptionLocalization[cmd.name.lowercase()]!!
                locale.value to argDescLocalization.getText(it.name.lowercase())
            }.toMap()

            it.setDescriptionLocalizations(argDescLocalizations)
        }

        commandData += command
    }

    event.jda.updateCommands().addCommands(commandData).queue()
}