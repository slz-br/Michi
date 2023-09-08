package michi.bot.commands.music.dj

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.net.URL
import java.util.concurrent.TimeUnit

@Suppress("Unused")
object ForcePlay: MichiCommand("fplay", GUILD_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Forces a track to be played immediately",
            DiscordLocale.ENGLISH_UK to "Forces a track to be played immediately",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Força que uma música seja tocada imediatamente"
        )

    override val userPermissions = listOf(Permission.ADMINISTRATOR)

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.VOICE_CONNECT,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/$name <search(the name or link of a music/playlist)>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument(
                name = "search",
                descriptionLocalization = mapOf(
                    DiscordLocale.ENGLISH_US to "The name/link of the track/playlist to play",
                    DiscordLocale.ENGLISH_UK to "The name/link of the track/playlist to play",
                    DiscordLocale.PORTUGUESE_BRAZILIAN to "O nome/link da música/playlist para tocar"
                ),
                type = OptionType.STRING
            )
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        val sender = context.user
        val guild = context.guild!!
        val musicManager = PlayerManager[guild]
        val scheduler = musicManager.scheduler
        val queue = scheduler.trackQueue
        val channel = guild.selfMember.voiceState!!.channel!!.asGuildMessageChannel()
        var trackURL = context.getOption("search")!!.asString

        if (!isURL(trackURL)) trackURL = "scsearch: $trackURL"

        val track = queue.last()

        PlayerManager.loadAndPlay(context, trackURL)

        scheduler.playTrackAt(queue.size - 1)

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val musicDjSuccess: YamlMap = success["music_dj"]!!

        channel.sendMessage(String.format(musicDjSuccess.getText("force_play"), sender.asMention, track.info.title, formatTrackLength(track)))
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
        val guild = context.guild!!
        val sender = context.member!!
        val bot = guild.selfMember
        val senderVoiceState = sender.voiceState!!
        val botVoiceState = bot.voiceState!!
        val guildDjMap = GuildDJMap.computeIfAbsent(guild) { mutableSetOf() }

        val err: YamlMap = getYML(sender.user).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val musicErr: YamlMap = err["music"]!!

        if (!sender.permissions.any(userPermissions::contains) && sender !in guildDjMap) {
            context.michiReply(String.format(genericErr.getText("user_missing_perms"), Emoji.michiBlep))
            return false
        }

        if (!botVoiceState.inAudioChannel()) {
            context.michiReply(musicErr.getText("bot_not_in_vc"))
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        if (!senderVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        if (!botVoiceState.inAudioChannel() && bot.hasAccess(senderVoiceState.channel!!)) {
            val audioManager = guild.audioManager
            val channelToJoin = senderVoiceState.channel

            audioManager.openAudioConnection(channelToJoin)
            audioManager.isSelfDeafened = true
        }

        if (!botVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        return true
    }

    private fun isURL(possibleURL: String): Boolean = try { URL(possibleURL); true } catch (e: Exception) { false }

}