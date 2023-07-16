package michi.bot.commands.music

import michi.bot.commands.*
import michi.bot.lavaplayer.PlayerManager
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
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
object Play: MichiCommand("play", "plays the track/playlist that you searched for.", CommandScope.GUILD_SCOPE) {

    private const val searchPrefix = "scsearch:"

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.VOICE_CONNECT,
            Permission.VOICE_SPEAK,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("search", "the name/link of a song/playlist.", OptionType.STRING)
        )

    override val usage: String
        get() = "/play <search(the name or link of a song/playlist)>"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        var searchedTrack = context.getOptionsByName("search")[0].asString

        if (!canHandle(context)) return

        if (!isURL(searchedTrack)) searchedTrack = "$searchPrefix:$searchedTrack"

        PlayerManager.loadAndPlay(context, searchedTrack)

        // puts the user that sent the command in cooldown
        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild!!
        val bot = guild.selfMember
        val botVoiceState = bot.voiceState!!
        val sender = context.member!!
        val senderVoiceState = sender.voiceState!!
        val search = context.getOption("search")!!.asString

        if (!bot.permissions.containsAll(botPermissions)) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!senderVoiceState.inAudioChannel()) {
            context.reply("You need to be in a voice channel to use this command ${Emoji.michiBlep}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!botVoiceState.inAudioChannel()) {
            val audioManager = guild.audioManager
            val channelToJoin = senderVoiceState.channel

            if (channelToJoin == null) {
                context.reply("Something went really wrong ${Emoji.michiOpsie}")
                    .setEphemeral(true)
                    .queue()
                return false
            }

            audioManager.openAudioConnection(channelToJoin)
        }

        if (isURL(search)) {
            if (search.contains("youtube.com/") || search.contains("youtu.be/")) {
                context.reply("I can't play musics from youtube ${Emoji.smolMichiAngry}")
                    .setEphemeral(true)
                    .queue()
                return false
            }
        }

        if (senderVoiceState.channel?.id != botVoiceState.channel?.id) {
            context.reply("You need to be in the same voice channel as me to use this command")
                .setEphemeral(true)
                .queue()
            return false
        }

        return true
    }

    private fun isURL(possibleURL: String): Boolean = try { URL(possibleURL); true } catch (e: Exception) { false }

}