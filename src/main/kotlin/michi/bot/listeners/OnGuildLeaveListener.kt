package michi.bot.listeners

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import michi.bot.database.dao.GuildsDAO
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object OnGuildLeaveListener: ListenerAdapter() {

    override fun onGuildLeave(event: GuildLeaveEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            // remove the guild from the database
            GuildsDAO.delete(event.guild)
        }
    }

}