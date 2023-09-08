package michi.bot.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import michi.bot.Michi
import michi.bot.commands.CommandScope
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

suspend fun updateGuildCommands(guild: Guild) {

    val commandData: MutableList<CommandData> = ArrayList()

    Mutex().withLock {

        Michi.commandList.forEach { cmd ->

            if (cmd.scope != CommandScope.GUILD_SCOPE) return@forEach

            val command = Commands.slash(cmd.name.lowercase(), cmd.descriptionLocalization[DiscordLocale.ENGLISH_US]!!)

            command.setDescriptionLocalizations(cmd.descriptionLocalization)

            for (arg in cmd.arguments) {

                command.addOption(
                    arg.type,
                    arg.name.lowercase(),
                    arg.descriptionLocalization[DiscordLocale.ENGLISH_US] ?: "404 - NOT FOUND",
                    arg.isRequired,
                    arg.hasAutoCompletion
                ).setDescriptionLocalizations(arg.descriptionLocalization)

            }

            commandData += command
        }
        guild.updateCommands().addCommands(commandData).queue()
    }
}

fun updateCommands(event: ReadyEvent) {
    val commandData: MutableList<CommandData> = ArrayList()

    Michi.commandList.forEach { cmd ->

        if (cmd.scope != CommandScope.GLOBAL_SCOPE) return@forEach

        val command = Commands.slash(cmd.name.lowercase(), cmd.descriptionLocalization[DiscordLocale.ENGLISH_US]!!)

        command.setDescriptionLocalizations(cmd.descriptionLocalization)

        for (arg in cmd.arguments) {

            command.addOption(
                arg.type,
                arg.name.lowercase(),
                arg.descriptionLocalization[DiscordLocale.ENGLISH_US] ?: "404 - NOT FOUND",
                arg.isRequired,
                arg.hasAutoCompletion
            ).setDescriptionLocalizations(arg.descriptionLocalization)

        }

        commandData += command
    }

    event.jda.updateCommands().addCommands(commandData).queue()
}