package michi.bot.listeners

import kotlinx.coroutines.*
import michi.bot.Michi.Companion.commandList
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

import michi.bot.database.dao.*
import michi.bot.util.Emoji
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

/**
 * Called whenever a slashCommand is used.
 * @author Slz
 */
object SlashCommandListener: ListenerAdapter() {
    private const val DELAY = (1000 * 5.25).toLong()

    private val cooldownList = mutableSetOf<User>()

    suspend fun cooldownManager(user: User) {
        cooldownList += user
        delay(DELAY)
        cooldownList -= user
    }

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
                if (channel is TextChannel && channel.asTextChannel().isNSFW) {
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