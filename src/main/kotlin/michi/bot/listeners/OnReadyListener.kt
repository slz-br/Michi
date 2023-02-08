package michi.bot.listeners

import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

class OnReadyListener: ListenerAdapter() {

    override fun onGuildReady(event: GuildReadyEvent) {
        val commandData: MutableList<CommandData> = ArrayList()

        // math
        commandData.add(Commands.slash("math", "gives you a basic math problem."))

        // ban
        commandData.add(Commands.slash(
            "ban", "bans the mentioned users")
            .addOption(OptionType.USER,"user1","the 1st user to ban", true)
            .addOption(OptionType.USER,"user2","the 2nd user to ban", false)
            .addOption(OptionType.USER,"user3","the 3rd user to ban", false)
            .addOption(OptionType.USER,"user4","the 4th user to ban", false)
            .addOption(OptionType.USER,"user5","the 5th user to ban", false)
            .addOption(OptionType.STRING,"reason","the reason for the ban", false)
        )

        // refresh the commands
        event.guild.updateCommands().queue()
        event.guild.updateCommands().addCommands(commandData).queue()
    }
}