package michi.bot.listeners

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import michi.bot.database.dao.GuildDAO
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Class that holds the event handler [onGuildLeave].
 */
object OnGuildLeaveListener: ListenerAdapter() {

    /**
     * Called whenever the bot leaves a guild
     * @author Slz
     */
    override fun onGuildLeave(event: GuildLeaveEvent) {
        CoroutineScope(IO).launch {
            // remove the guild from the database
            GuildDAO.delete(event.guild)
        }
    }

}