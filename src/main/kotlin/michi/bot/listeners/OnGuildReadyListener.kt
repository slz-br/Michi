package michi.bot.listeners

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import michi.bot.database.dao.BlacklistDAO
import michi.bot.database.dao.GuildsDAO
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.updateGuildCommands
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Object that holds the event handler [onGuildReady].
 * @author Slz
 */
object OnGuildReadyListener: ListenerAdapter() {

    /**
     * Called whenever a guild is loaded.
     * @author Slz
     */
    override fun onGuildReady(event: GuildReadyEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild

            // if the guild is tagged as nsfw, then leave.
            if (guild.nsfwLevel == Guild.NSFWLevel.EXPLICIT || guild.nsfwLevel == Guild.NSFWLevel.AGE_RESTRICTED) {
                BlacklistDAO.post(guild = guild, reason = "Discord tagged server as NSFW.")
                guild.leave().queue()
                return@launch
            }

            // retrieve the music queue of the queue and add it to the queue.
            PlayerManager.retrieveGuildMusicQueue(guild)

            // register guild commands
            updateGuildCommands(guild)

            // add the guild to the database if it isn't already in the database.
            GuildsDAO.post(guild)

        }
    }

}