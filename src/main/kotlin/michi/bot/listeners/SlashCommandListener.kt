package michi.bot.listeners

import kotlinx.coroutines.delay
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.entities.User
import java.io.BufferedReader
import java.io.FileReader

import michi.bot.commands.admin.*
import michi.bot.commands.mail.*
import michi.bot.commands.math.*
import michi.bot.commands.misc.*
import michi.bot.commands.util.*
import michi.bot.util.Emoji

private const val DELAY = (1000 * 5.5).toLong()

/**
 * Called whenever a slashCommand is used.
 * @author Slz
 */
class SlashCommandListener: ListenerAdapter() {
    companion object {

        private val cooldownList = mutableSetOf<User>()
        suspend fun cooldownManager(user: User) {
            cooldownList.add(user)
            delay(DELAY)
            cooldownList.remove(user)
        }

    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val blackList = BufferedReader(FileReader("BlackList.txt")).readLines()
        val name = event.name
        val sender = event.user
        val guild = event.guild ?: return

        // Checks if the user or guild is blacklisted
        if (blackList.contains(sender.id) || blackList.contains(guild.id)) {
            event.reply("You can't use my commands anymore ${Emoji.michiTroll}").setEphemeral(true).queue()
            return
        }

        // Checks if the user is in cooldown
        if (cooldownList.contains(sender)) {
            event.reply("You are in cooldown, wait a bit ${Emoji.michiSip}").setEphemeral(true).queue()
            return
        }

        // if everything is right, try to execute the command
        GlobalScope.launch {
            when (name) {

                /* Admin Commands */
                "ban" ->      Ban.execute(event)
                "mute" ->     Mute.execute(event)
                "unban" ->    UnBan.execute(event)
                "clear" ->    Clear.execute(event)
                "slowmode" -> SlowMode.execute(event)

                /* Mail Commands */
                "mail" ->        Mail.sendMail(event)
                "mail-window" -> Mail.writeMail(event)
                "inbox" ->       Mail.inbox(event)
                "read" ->        Mail.read(event)
                "remove" ->      Mail.remove(event)
                "clear-inbox" -> Mail.clearInbox(event)
                "report-mail" -> Mail.report(event)

                /* Misc Commands*/
                "wiki" ->    Wiki.execute(event)
                "raccoon" -> Raccoon.execute(event)
                "math" ->    Math.execute(event)

                /* Util */
                "help" -> help(event)

            }

        }

    }

}