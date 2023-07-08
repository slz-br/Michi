package michi.bot.listeners

import kotlinx.coroutines.*
import michi.bot.Michi
import michi.bot.commands.CommandScope
import michi.bot.database.dao.BlacklistDAO
import michi.bot.database.dao.GuildsDAO
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

object OnGuildJoin: ListenerAdapter() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onGuildJoin(event: GuildJoinEvent) {
        GlobalScope.launch {
            val guild = event.guild

            if (BlacklistDAO.find(guild)) guild.leave().queue()

            if (guild.nsfwLevel == Guild.NSFWLevel.EXPLICIT || guild.nsfwLevel == Guild.NSFWLevel.AGE_RESTRICTED) {
                BlacklistDAO.post(guild, "Discord tagged server as NSFW.")
                return@launch
            }

            val commandData: MutableList<CommandData> = ArrayList()

            Michi.commandList.forEach { cmd ->

                if (cmd.scope == CommandScope.GUILD_SCOPE) {

                    val command = Commands.slash(cmd.name.lowercase(), cmd.description)
                    for (arg in cmd.arguments) {
                        command.addOption(
                            arg.type,
                            arg.name.lowercase(),
                            arg.description,
                            arg.isRequired,
                            arg.hasAutoCompletion
                        )
                    }

                    commandData += command
                }

            }

            GuildsDAO.post(guild)
        }
    }

}