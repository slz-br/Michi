package michi.bot.listeners

import michi.bot.Michi.Companion.commandList
import michi.bot.commands.CommandScope
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

object OnReadyListener: ListenerAdapter() {

    override fun onReady(event: ReadyEvent) {

        // registering global commands
        val commandData: MutableList<CommandData> = ArrayList()

        commandList.forEach { cmd ->

            if (cmd.scope == CommandScope.GLOBAL_SCOPE) {
                val command = Commands.slash(cmd.name.lowercase(), cmd.description)
                for (arg in cmd.arguments) {
                    command.addOption(
                        arg.type,
                        arg.name.lowercase(),
                        arg.description,
                        arg.isRequired,
                        arg.hasAutoCompletion
                    )
                }

                commandData += command
            }
        }

        event.jda.updateCommands().addCommands(commandData).queue()
    }
}