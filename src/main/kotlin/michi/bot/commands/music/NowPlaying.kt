package michi.bot.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import michi.bot.commands.CommandScope
import michi.bot.commands.MichiCommand
import michi.bot.lavaplayer.PlayerManager
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.util.concurrent.TimeUnit

@Suppress("Unused")
object NowPlaying: MichiCommand("np", "gives you the track that the bot is playing", CommandScope.GUILD_SCOPE) {

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/np"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val guild = context.guild ?: return
        val sender = context.user

        val playingTrack = PlayerManager.getMusicManager(guild).getPlayingTrack()

        val embed = EmbedBuilder()
            .setColor(Color.MAGENTA)
            .addField(
                "${playingTrack.info.title}[${formatTrackLength(playingTrack)}]",
                "uploaded by: ${playingTrack.info.author}",
                false
            )
            .setFooter("url: ${playingTrack.info.uri}")

        context.replyEmbeds(embed.build())
            .setEphemeral(true)
            .queue()

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild ?: return false
        val musicManager = PlayerManager.getMusicManager(guild)
        val queue = musicManager.scheduler.trackQueue
        val bot = guild.selfMember
        val sender = context.member ?: return false
        val senderVoiceState = sender.voiceState ?: return false
        val botVoiceState = bot.voiceState ?: return false

        if (!bot.permissions.containsAll(botPermissions)) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (queue.isEmpty()) {
            context.reply("The queue is empty, so there is nothing playing rn ${Emoji.michiSad}")
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

        if (senderVoiceState.channel != botVoiceState.channel) {
            context.reply("You need to be in the same voice channel as me to use this command")
                .setEphemeral(true)
                .queue()
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

}