package michi.bot.listeners

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.entities.User
import java.io.BufferedReader
import java.io.FileReader

/**
 * Called whenever there's a SlashCommandInteractionEvent
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
    @OptIn(DelicateCoroutinesApi::class)
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val blackList = BufferedReader(FileReader("BlackList.txt")).readLines()
        val name = event.name
        val sender = event.user
        val guild = event.guild ?: return

        if (blackList.contains(sender.id) || blackList.contains(guild.id)) {
            event.reply("You can't use my commands anymore ${Emoji.michiTroll}")
                .setEphemeral(true)
                .queue()
            return
        }

        // Checks if the user is in cooldown
        if (cooldownList.contains(sender)) {
            event.reply("You are in cooldown, wait a bit ${Emoji.michiSip}")
                .setEphemeral(true)
                .queue()
            return
        }

        // puts the user in cooldown
        GlobalScope.launch { cooldownManager(sender) }

        // if everything is right, try to execute the command
        when (name) {
            "math"    -> MathProblem.tryToExecute(event)
            "ban"     -> Ban.tryToExecute(event)
            "unban"   -> UnBan.tryToExecute(event)
            "wiki"    -> randomWiki(event)
            "help"    -> help(event)
            "raccoon" -> randomRaccoon(event)
        }

    }

}