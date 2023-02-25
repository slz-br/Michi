package michi.bot.listeners

import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

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

        // join
        commandData.add(Commands.slash("join", "joins your current voice channel if possible"))

        // play
        commandData.add(Commands.slash("play", "plays the selected youtube song")
            .addOption(OptionType.STRING, "song", "link or name of a youtube video", true)
        )

        // stop
        commandData.add(Commands.slash("stop", "stops the music and clear the queue"))

        // skip
        commandData.add(Commands.slash("skip", "starts to play the next music on the queue"))

        // wiki
        commandData.add(Commands.slash("wiki", "gives you a random wikipedia article"))

        // help
        commandData.add(Commands.slash("help", "relevant info about michi"))

        // mail
        commandData.add(Commands.slash("mail", "send an anonymous message to an user")
            .addOption(OptionType.USER, "receiver", "the user that you want to send the mail", true)
            .addOption(OptionType.STRING, "message", "the message that you want to send", true)
        )

        // queue
        commandData.add(Commands.slash("queue", "gets the queue")
            .addOption(OptionType.INTEGER, "page", "the page of the queue", true)
        )

        // np
        commandData.add(Commands.slash("np", "gets the current playing track"))

        // jumpTo
        commandData.add(Commands.slash("jump", "plays a determined track of the queue")
            .addOption(OptionType.INTEGER, "position", "the position in the queue of the track", true)
        )

        // raccoon
        commandData.add(Commands.slash("raccoon", "gives you a raccoon pic or gif"))

        event.guild.updateCommands().addCommands(commandData).queue()
    }
}