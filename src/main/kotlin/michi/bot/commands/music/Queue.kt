package michi.bot.commands.music

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
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.awt.Color
import java.util.concurrent.TimeUnit

@Suppress("Unused")
object Queue: MichiCommand("queue", GUILD_SCOPE) {

    private const val TRACKS_PER_PAGE = 5

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/$name <page(optional)>"

    override val arguments = listOf(MichiArgument("page", OptionType.INTEGER, isRequired = false))

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val page = if (context.getOption("page") != null) context.getOption("page")!!.asInt - 1 else 0
        val guild = context.guild ?: return
        val musicManager = PlayerManager[guild]
        val queue = musicManager.scheduler.trackQueue
        val player = musicManager.player
        val pagesCount = if (queue.size != 0 && queue.size / TRACKS_PER_PAGE != 0) queue.size / TRACKS_PER_PAGE else 1

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val musicSuccess: YamlMap = success["music"]!!
        val other: YamlMap = getYML(context).yamlMap["other"]!!
        val musicOther: YamlMap = other["music"]!!
        val otherGeneric: YamlMap = other["generic"]!!

        if (!canHandle(context)) return

        val embed = EmbedBuilder().apply {
            setColor(Color.MAGENTA)
            setTitle(musicOther.getText("queue"))
            setFooter(guild.name, guild.iconUrl)
        }

        val emptyQueueMessage = musicOther.getText("custom_empty_queue").split("\n")
        if (queue.isEmpty() && player.playingTrack == null) {
            embed.addField(emptyQueueMessage[0], String.format(emptyQueueMessage[1], Emoji.michiMusic), false)
            context.michiReply(embed.build())
            return
        }

        val playingTrack = player.playingTrack
        val playingTrackInfo = playingTrack.info ?: null

        playingTrackInfo?.let {
            embed.addField(
                musicSuccess.getText("queue_current_playing_track"),
                "${playingTrackInfo.title}`[${formatTrackLength(playingTrack)}]`\n`${playingTrackInfo.uri}`",
                false
            )
        }

        for (i in 0 until TRACKS_PER_PAGE) {
            if (i > queue.size - 1) break

            val track = queue.elementAt(page * TRACKS_PER_PAGE + i)
            val trackInfo = track.info

            embed.addField("#${(page * TRACKS_PER_PAGE + i) + 1} ${trackInfo.title}`[${formatTrackLength(track)}]`",
                String.format(musicOther.getText("uploaded_by"), trackInfo.author),
                false
            )
        }

        embed.setFooter(String.format(otherGeneric.getText("page_count"), page + 1, pagesCount))

        context.michiReply(embed.build())
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
        val bot = guild.selfMember
        val sender = context.member!!
        val senderVoiceState = sender.voiceState!!
        val queue = PlayerManager[guild].scheduler.trackQueue
        val page = if (context.getOption("page") != null) context.getOption("page")!!.asInt - 1 else 0
        val pagesCount = if (queue.size != 0 && queue.size / TRACKS_PER_PAGE != 0) queue.size / TRACKS_PER_PAGE else 1

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val musicErr: YamlMap = err["music"]!!

        if (page > pagesCount || page < 0) {
            context.michiReply(genericErr.getText("invalid_page"))
            return false
        }

        if (!senderVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        return true
    }
}