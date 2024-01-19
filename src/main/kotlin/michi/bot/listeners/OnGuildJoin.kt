package michi.bot.listeners

import kotlinx.coroutines.*
import michi.bot.database.dao.BlacklistDao
import michi.bot.database.dao.GuildDao
import michi.bot.util.updateGuildCommands
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Class that holds the event handler [onGuildJoin].
 * @author Slz
 */
object OnGuildJoin: ListenerAdapter() {

    /**
     * Called whenever the bot joins a guild.
     * @author Slz
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun onGuildJoin(event: GuildJoinEvent) {
        GlobalScope.launch {
            val guild = event.guild

            // If the guild is tagged as nsfw, then blacklist it.
            if (guild.nsfwLevel == Guild.NSFWLevel.EXPLICIT || guild.nsfwLevel == Guild.NSFWLevel.AGE_RESTRICTED) {
                BlacklistDao.post(guild, "Discord tagged server as NSFW.")
                return@launch
            }

            // If the guild is in the blacklist, then leave.
            if (BlacklistDao.find(guild)) guild.leave().queue()

            // register guild commands
            updateGuildCommands(guild)

            // add the guild to the database
            GuildDao.post(guild)
        }
    }

}