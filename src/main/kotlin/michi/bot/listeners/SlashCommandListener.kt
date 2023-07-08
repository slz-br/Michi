package michi.bot.listeners

import kotlinx.coroutines.*
import michi.bot.Michi.Companion.commandList
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

import michi.bot.commands.admin.*
import michi.bot.commands.mail.*
import michi.bot.commands.math.*
import michi.bot.commands.misc.*
import michi.bot.commands.util.*
import michi.bot.util.Emoji

private const val DELAY = (1000 * 5.25).toLong()

/**
 * Called whenever a slashCommand is used.
 * @author Slz
 */
object SlashCommandListener: ListenerAdapter() {

    private val cooldownList = mutableSetOf<User>()

    suspend fun cooldownManager(user: User) {
        cooldownList.add(user)
        delay(DELAY)
        cooldownList.remove(user)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val name = event.name
            val sender = event.user
            val guild = event.guild
            val channel = event.channel

            // Checks if the user or guild is blacklisted
            if (BlacklistDAO.find(sender) || BlacklistDAO.find(guild)) {
                event.reply("You can't use my commands anymore ${Emoji.michiTroll}")
                    .setEphemeral(true)
                    .queue()
                return@launch
            }

            // If the guild somehow isn't in the database, put it in the database
            if (GuildsDAO.get(event.guild) == null) guild?.let { GuildsDAO.post(it) }

            guild?.let {
                if (channel.asTextChannel().isNSFW) {
                    event.reply("You can't use my commands is nsfw channels.")
                        .setEphemeral(true)
                        .queue()
                    return@launch
                }
            }

            // Checks if the user is in cooldown
            if (cooldownList.contains(sender)) {
                event.reply("You're in cooldown, wait a bit ${Emoji.michiSip}")
                    .setEphemeral(true)
                    .queue()
                return@launch
            }

            commandList.forEach {
                if (name == it.name) it.execute(event)
            }

        }

    }

}