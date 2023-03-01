package michi.bot.listeners

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

        // math
        commandData.add(Commands.slash("math", "gives you a basic math problem."))

        // ban
        commandData.add(Commands.slash("ban", "bans the mentioned users")
            .addOption(OptionType.USER,"user1","the 1st user to ban", true)
            .addOption(OptionType.USER,"user2","the 2nd user to ban", false)
            .addOption(OptionType.USER,"user3","the 3rd user to ban", false)
            .addOption(OptionType.USER,"user4","the 4th user to ban", false)
            .addOption(OptionType.USER,"user5","the 5th user to ban", false)
            .addOption(OptionType.STRING,"reason","the reason for the ban", false)
        )

        // unban
        commandData.add(Commands.slash("unban", "bans the mentioned users")
            .addOption(OptionType.USER,"user1","the 1st user to ban", true)
            .addOption(OptionType.USER,"user2","the 2nd user to ban", false)
            .addOption(OptionType.USER,"user3","the 3rd user to ban", false)
            .addOption(OptionType.USER,"user4","the 4th user to ban", false)
            .addOption(OptionType.USER,"user5","the 5th user to ban", false)
        )

        // wiki
        commandData.add(Commands.slash("wiki", "gives you a random wikipedia article"))

        // help
        commandData.add(Commands.slash("help", "relevant info about michi"))

        // mail
        commandData.add(Commands.slash("mail", "send an anonymous message to an user")
            .addOption(OptionType.USER, "receiver", "the user that you want to send the mail", true)
            .addOption(OptionType.STRING, "message", "the message that you want to send", true)
        )

        // raccoon
        commandData.add(Commands.slash("raccoon", "gives you a raccoon pic or gif"))

        event.guild.updateCommands().addCommands(commandData).queue()
    }
}