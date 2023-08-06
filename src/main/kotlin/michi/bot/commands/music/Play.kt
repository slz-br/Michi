package michi.bot.commands.music

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap

import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.*
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.net.URL

/**
 * Object for the "play" command, a command that searches for a track/playlist in soundcloud and
 * stream it in a voice channel.
 * @author Slz
 * @see execute
 */
@Suppress("Unused")
object Play: MichiCommand("play", GUILD_SCOPE) {

    private const val SEARCH_PREFIX = "scsearch:"

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.VOICE_CONNECT,
            Permission.VOICE_SPEAK,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val arguments = listOf(MichiArgument("search", OptionType.STRING))

    override val usage: String
        get() = "/$name <search(the name or link of a song/playlist)>"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        var searchedTrack = context.getOptionsByName("search")[0].asString

        if (!canHandle(context)) return

        if (!isURL(searchedTrack)) searchedTrack = "$SEARCH_PREFIX:$searchedTrack"

        PlayerManager.loadAndPlay(context, searchedTrack)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild!!
        val bot = guild.selfMember
        val botVoiceState = bot.voiceState!!
        val sender = context.member!!
        val senderVoiceState = sender.voiceState!!
        val search = context.getOption("search")!!.asString

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val musicErr: YamlMap = err["music"]!!

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        if (!senderVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        if (!botVoiceState.inAudioChannel()) {
            val audioManager = guild.audioManager
            val channelToJoin = senderVoiceState.channel

            audioManager.openAudioConnection(channelToJoin)
            audioManager.isSelfDeafened = true
        }

        if (isURL(search)) {
            if (search.contains("youtube.com/") || search.contains("youtu.be/")) {
                context.michiReply(String.format(musicErr.getText("cant_stream_from_yt"), Emoji.smolMichiAngry))
                return false
            }
        }

        if (senderVoiceState.channel?.id != botVoiceState.channel?.id) {
            context.michiReply(musicErr.getText("user_not_in_bot_vc"))
            return false
        }

        return true
    }

    private fun isURL(possibleURL: String): Boolean = try { URL(possibleURL); true } catch (e: Exception) { false }

}