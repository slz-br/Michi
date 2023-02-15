package michi.bot.listeners

import michi.bot.util.CommandManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import michi.bot.util.Emoji
import java.io.BufferedReader
import java.io.FileReader

/**
 * Called whenever there's a SlashCommandInteractionEvent
 * @author Slz
 */

class SlashCommandListener: ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val name = event.name
        val sender = event.user
        val guild = event.guild ?: return

        if (blackList.contains(sender.id) || blackList.contains(guild.id)) {
            event.reply("You can't use my commands anymore ${Emoji.michiTroll}")
                .setEphemeral(true)
                .queue()
            return
        }

        // if everything is right
        when (name) {
            "math" -> CommandManager.checkMath(event)
            "ban" -> CommandManager.checkBan(event) // it can only ban people that are in the server.
            "unban" -> CommandManager.checkUnban(event)
        }

    }

}