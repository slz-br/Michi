package michi.bot.listeners

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import michi.bot.Michi.Companion.commandList
import michi.bot.database.dao.BlacklistDAO
import michi.bot.database.dao.GuildDAO
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Object that holds the event handler [onSlashCommandInteraction].
 * @author Slz
 */
object CommandListener: ListenerAdapter() {
    private const val DELAY = (1000 * 5.25).toLong()

    private val cooldownList = mutableSetOf<User>()
    private val mutex = Mutex()
    val json = Json { ignoreUnknownKeys = true }

    private suspend fun cooldownManager(user: User) {
        cooldownList += user
        delay(DELAY)
        cooldownList -= user
    }
    /**
     * Called whenever a slashCommand is used.
     * @author Slz
     */
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        CoroutineScope(IO).launch {
            val name = event.name
            val sender = event.user
            val guild = event.guild
            val channel = event.channel
            val errMsg: YamlMap = getYML(event).yamlMap["error_messages"]!!
            val genericErr: YamlMap = errMsg["generic"]!!

            // Checks if the user or guild is blacklisted
            if (BlacklistDAO.find(sender) || BlacklistDAO.find(guild)) {
                event.michiReply(String.format(genericErr.getText("user_blacklisted"), Emoji.michiTroll))
                return@launch
            }

            // If the guild somehow isn't in the database, put it in the database
            if (GuildDAO.get(event.guild) == null) guild?.let { GuildDAO.post(it) }

            guild?.let {

                if (channel is TextChannel && channel.isNSFW) {
                    event.michiReply(genericErr.getText("cant_use_commands_in_nsfw_channel"))
                    return@launch
                }

            }

            // Checks if the user is in cooldown
            if (sender in cooldownList) {
                event.michiReply(String.format(genericErr.getText("user_in_command_cooldown"), Emoji.michiSip))
                return@launch
            }

            commandList.find { name == it.name }?.execute(event)

            mutex.withLock { cooldownManager(sender) }
        }

    }

}