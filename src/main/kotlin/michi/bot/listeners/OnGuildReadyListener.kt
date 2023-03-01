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
        commandData.add(Commands.slash("math", "Gives you a basic math problem."))

        // ban
        commandData.add(Commands.slash("ban", "Bans the mentioned users")
            .addOption(OptionType.USER,"user1","The 1st user to ban", true)
            .addOption(OptionType.USER,"user2","The 2nd user to ban", false)
            .addOption(OptionType.USER,"user3","The 3rd user to ban", false)
            .addOption(OptionType.USER,"user4","The 4th user to ban", false)
            .addOption(OptionType.USER,"user5","The 5th user to ban", false)
            .addOption(OptionType.STRING,"reason","The reason for the ban", false)
        )

        // unban
        commandData.add(Commands.slash("unban", "Bans the mentioned users")
            .addOption(OptionType.USER,"user1","The 1st user to unban", true)
            .addOption(OptionType.USER,"user2","The 2nd user to unban", false)
            .addOption(OptionType.USER,"user3","The 3rd user to unban", false)
            .addOption(OptionType.USER,"user4","The 4th user to unban", false)
            .addOption(OptionType.USER,"user5","The 5th user to unban", false)
        )

        // wiki
        commandData.add(Commands.slash("wiki", "Gives you a random wikipedia article"))

        // help
        commandData.add(Commands.slash("help", "Relevant info about michi"))

        // mail
        commandData.add(Commands.slash("mail", "Send an anonymous message to an user")
            .addOption(OptionType.USER, "receiver", "The user that you want to send the mail", true)
            .addOption(OptionType.STRING, "message", "The message that you want to send", true)
        )

        // raccoon
        commandData.add(Commands.slash("raccoon", "Gives you a raccoon pic or gif"))

        event.guild.updateCommands().addCommands(commandData).queue()
    }
}