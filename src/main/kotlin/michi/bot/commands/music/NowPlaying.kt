package michi.bot.commands.music

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.util.concurrent.TimeUnit

@Suppress("Unused")
object NowPlaying: MichiCommand("np", GUILD_SCOPE) {

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.VOICE_CONNECT,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val guild = context.guild ?: return
        val playingTrack = PlayerManager[guild].playingTrack

        if (!canHandle(context)) return

        if (playingTrack == null) {
            context.michiReply("Nothing playing rn")
            return
        }

        val embed = EmbedBuilder().apply {
            setTitle("**${playingTrack.info.title}**`[${formatTrackLength(playingTrack)}]`", playingTrack.info.uri)
            setColor(Color.MAGENTA)
        }

        val success: YamlMap = getYML(context).yamlMap["other"]!!
        val musicOther: YamlMap = success["music"]!!

        context.michiReply(embed.build(), message = musicOther.getText("now_playing"))
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild ?: return false
        val bot = guild.selfMember
        val sender = context.member ?: return false
        val senderVoiceState = sender.voiceState ?: return false
        val botVoiceState = bot.voiceState ?: return false

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

        if (senderVoiceState.channel != botVoiceState.channel) {
            context.michiReply(musicErr.getText("user_not_in_bot_vc"))
            return false
        }

        return true
    }

    private fun formatTrackLength(track: AudioTrack): String {
        val timeInMiliss = track.duration

        val hours = timeInMiliss / TimeUnit.HOURS.toMillis(1)
        val minutes = timeInMiliss / TimeUnit.MINUTES.toMillis(1)
        val seconds = timeInMiliss % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1)

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun formatTrackLength(timeInMiliss: Long): String {
        val hours = timeInMiliss / TimeUnit.HOURS.toMillis(1)
        val minutes = timeInMiliss / TimeUnit.MINUTES.toMillis(1)
        val seconds = timeInMiliss % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1)

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

}