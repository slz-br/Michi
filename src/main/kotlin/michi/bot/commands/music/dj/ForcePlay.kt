package michi.bot.commands.music.dj

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.net.URL
import java.util.concurrent.TimeUnit

@Suppress("Unused")
object ForcePlay: MichiCommand("fplay", "Forces a track to be played.", CommandScope.GUILD_SCOPE) {

    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR
        )

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/fplay <search(the name or link of a music/playlist)>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("search", "The name of the track to play", OptionType.STRING, isRequired = true, hasAutoCompletion = false)
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val guild = context.guild!!
        val musicManager = PlayerManager.getMusicManager(guild)
        val scheduler = musicManager.scheduler
        val queue = scheduler.trackQueue
        val channel = context.channel
        var trackURL = context.getOption("search")!!.asString

        if (!canHandle(context)) return

        if (!isURL(trackURL)) trackURL = "scsearch:$trackURL"

        PlayerManager.loadAndPlay(context, trackURL)
        scheduler.playTrackAt(queue.size - 1)

        context.reply("Ok. Here is the track ${Emoji.michiThumbsUp}")
            .setEphemeral(true)
            .queue()

        val track = queue.elementAt(queue.size-1)
        channel.sendMessage("${sender.asMention} force played ${track.info.title}`[${formatTrackLength(track)}]`")
            .queue()
    }

    private fun formatTrackLength(track: AudioTrack): String {
        val timeInMiliss = track.duration

        val hours = timeInMiliss / TimeUnit.HOURS.toMillis(1)
        val minutes = timeInMiliss / TimeUnit.MINUTES.toMillis(1)
        val seconds = timeInMiliss % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1)

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild ?: return false
        val sender = context.member ?: return false
        val bot = guild.selfMember
        val channel = context.channel.asTextChannel()
        val senderVoiceState = sender.voiceState!!
        val botVoiceState = bot.voiceState!!

        if (!sender.permissions.any { permission -> userPermissions.contains(permission) }) {
            context.reply("You don't have permission to use this command, silly you ${Emoji.michiBlep}")
                .setEphemeral(true)
                .queue()
            return false
        }

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

        if (senderVoiceState.channel != botVoiceState.channel) {
            context.reply("You need to be in the same voice channel as me to use this command")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!bot.hasPermission(channel)) {
            context.reply("I don't have permission to message in this channel")
                .setEphemeral(true)
                .queue()
            return false
        }

        return true
    }

    private fun isURL(possibleURL: String): Boolean = try { URL(possibleURL); true } catch (e: Exception) { false }

}