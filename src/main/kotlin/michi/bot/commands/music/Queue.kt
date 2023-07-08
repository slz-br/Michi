package michi.bot.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.lavaplayer.PlayerManager
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import michi.bot.util.Emoji as MichiEmoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Color
import java.util.concurrent.TimeUnit

@Suppress("Unused")
object Queue: MichiCommand("queue", "Gives you the queue of tracks of the server", CommandScope.GUILD_SCOPE) {

    private const val TRACKS_PER_PAGE = 5

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/queue <page(optional)>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("page", "the page of the queue to see", OptionType.INTEGER, isRequired = false, hasAutoCompletion = false)
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val page = if (context.getOption("page") != null) context.getOption("page")!!.asInt - 1 else 0
        val guild = context.guild ?: return
        val musicManager = PlayerManager.getMusicManager(guild)
        val queue = musicManager.scheduler.trackQueue
        val player = musicManager.player
        val pagesCount = if (queue.size != 0 && queue.size / TRACKS_PER_PAGE != 0) queue.size / TRACKS_PER_PAGE else 1
        val sender = context.user

        if (page > pagesCount || page < 0) {
            context.reply("Invalid page.")
                .setEphemeral(true)
                .queue()
            return
        }

        if (!canHandle(context)) return

        val embed = EmbedBuilder().apply {
            setColor(Color.MAGENTA)
            setTitle("Queue")
            setFooter(guild.name, guild.iconUrl)
        }

        if (queue.isEmpty() && player.playingTrack == null) {
            embed.addField("The queue is empty", "why don't you change this putting some music to play? ${MichiEmoji.michiMusic}", false)
            context.replyEmbeds(embed.build()).setEphemeral(true).queue()
            return
        }

        val playingTrack = player.playingTrack
        val playingTrackInfo = playingTrack.info

        embed.addField(
            "Current playing track:",
            "${playingTrackInfo.title}`[${formatTrackLength(playingTrack)}]`\n`${playingTrackInfo.uri}`",
            false
        )

        for (i in 0 until TRACKS_PER_PAGE) {
            if (i > queue.size - 1) break

            val track = queue.elementAt(page * TRACKS_PER_PAGE + i)
            val trackInfo = track.info

            embed.addField("#${(page * TRACKS_PER_PAGE + i) + 1} ${trackInfo.title}`[${formatTrackLength(track)}]`",
                "uploaded by: ${trackInfo.author}`",
                false
            )
        }

        embed.setFooter("page ${page + 1} of $pagesCount")

        val nextPageButton = Button.primary("queue-next-page", "Next Page")
        val previousPageButton = Button.primary("queue-previous-page", "Previous Page")

        val reply = context.replyEmbeds(embed.build()).setEphemeral(true)

        if (page + 1 <= queue.size - 1) reply.addActionRow(nextPageButton)
        if (page - 1 >= 0) reply.addActionRow(previousPageButton)

        reply.setEphemeral(true).queue()

        // puts the user that sent the command in cooldown
        SlashCommandListener.cooldownManager(sender)
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

        if (!senderVoiceState.inAudioChannel()) {
            context.reply("You need to be in a voice channel to use this command ${Emoji.michiBlep}")
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

        return true
    }
}