package michi.bot.listeners

import kotlinx.coroutines.*
import michi.bot.Michi
import michi.bot.commands.CommandScope
import michi.bot.database.dao.BlacklistDAO
import michi.bot.database.dao.GuildsDAO
import michi.bot.lavaplayer.PlayerManager
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

/**
 * Called whenever a guild is loaded.
 * @author Slz
 */
object OnGuildReadyListener: ListenerAdapter() {

    override fun onGuildReady(event: GuildReadyEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            // Putting the guild in the database
            GuildsDAO.post(guild)
            PlayerManager.retrieveGuildMusicQueue(guild)

            if (guild.nsfwLevel == Guild.NSFWLevel.EXPLICIT || guild.nsfwLevel == Guild.NSFWLevel.AGE_RESTRICTED) {
                BlacklistDAO.post(guild = guild, reason = "Discord tagged server as NSFW.")
                guild.leave().queue()
                return@launch
            }

            // Registering guild commands
            val commandData = mutableSetOf<CommandData>()

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
                    commandData.add(command)

                }
            }
            guild.updateCommands().addCommands(commandData).queue()

        }
    }

}