package michi.bot.listeners

import michi.bot.Michi
import michi.bot.commands.CommandScope
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

/**
 * Called whenever a guild is loaded.
 * @author Slz
 */
class OnGuildReadyListener: ListenerAdapter() {

    override fun onGuildReady(event: GuildReadyEvent) {
        val commandData: MutableList<CommandData> = ArrayList()

        // help
        commandData.add(Commands.slash("help", "Relevant info about michi."))

        // mail
        commandData.add(Commands.slash("mail", "Send an anonymous message to an user.")
            .addOption(OptionType.STRING, "title", "What's the mail about?", true)
            .addOption(OptionType.STRING, "message", "The content/body of the mail.", true)
            .addOption(OptionType.USER, "receiver", "Who are you sending this mail to?", true)
        )

        // mail-window
        commandData.add(Commands.slash("mail-window", "Send an anonymous message to an user but using a window."))

        // inbox
        commandData.add(Commands.slash("inbox", "Check the mails you received.")
            .addOption(OptionType.INTEGER, "page", "The inbox page you want to see.", true)
        )

        // read
        commandData.add(Commands.slash("read", "Read an mail from your inbox.")
            .addOption(OptionType.INTEGER, "position", "The position of the email you want to read in your inbox.", true)
        )

        // remove
        commandData.add(Commands.slash("remove", "Remove an email from your inbox.")
            .addOption(OptionType.INTEGER, "position", "The position of the mail you want to remove in your inbox.", true)
        )

        Michi.commandList.forEach { cmd ->

            if (cmd.scope == CommandScope.GUILD_SCOPE) {

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

                commandData.add(command)
            }
        }

        // report
        commandData.add(Commands.slash("report-mail", "Reports a mail that was sent to you.")
            .addOption(OptionType.INTEGER, "mail-position", "The position of the mail in your inbox.")
        )

        // clearInbox
        commandData.add(Commands.slash("clear-inbox", "Clears your mails inbox."))

        event.guild.updateCommands().addCommands(commandData).queue()
    }
}