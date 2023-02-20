package michi.bot.commands.music

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

/**
 * Contains all music related commands.
 * @author Slz
 */

abstract class MusicCommands: AudioEventAdapter() {

    companion object {

        /**
         * Function to make Michi join the member current channel.
         * @param context The SlashCommandInteractionEvent that called this function.
         * @author Slz
         * @see play
         */
        fun join(context: SlashCommandInteractionEvent) {
            val guild = context.guild!!
            val audioManager = guild.audioManager
            val channelToJoin = context.member!!.voiceState!!.channel!!.asVoiceChannel()

            // embed message
            val embed = EmbedBuilder()
            embed.setColor(Color.GREEN)
                .setTitle("Joined! ${Emoji.michiMusic}")
                .addField("Joined ${channelToJoin.name}", "put some music on! ${Emoji.michiNodders}", false)
            context.replyEmbeds(embed.build())
                .queue()

            audioManager.isSelfDeafened = true
            audioManager.openAudioConnection(channelToJoin)
        }

        /**
         * Function to make Michi play(or queue) a track.
         * @param context The SlashCommandInteractionEvent that called this function.
         * @author Slz
         * @see skip
         * @see stop
         */
        fun play(context: SlashCommandInteractionEvent, url: String) {
           PlayerManager.instance!!.loadAndPlay(context, url)
        }

        /**
         * Function to skip to the next track on the queue
         * @param context The SlashCommandInteractionEvent that called this function.
         * @author Slz
         * @see stop
         */
        fun skip(context: SlashCommandInteractionEvent) {
            val guild = context.guild!!
            val musicManager = PlayerManager.instance!!.getMusicManager(guild)
            musicManager.scheduler.nextTrack()
            context.reply("skiped").queue()
        }

        /**
         * Function to stop the current playing track and clear the queue.
         * @param context The SlashCommandInteractionEvent that called this function.
         * @author Slz
         * @see skip
         */
        fun stop(context: SlashCommandInteractionEvent) {
            val embed = EmbedBuilder()
            val musicManager = PlayerManager.instance!!.getMusicManager(context.guild!!)
            val sender = context.user

            embed.setColor(Color.BLUE)
                .setTitle("Stoped")
                .addField("${sender.name} cleared the queue", "No more music? ${Emoji.michiSad}", false)
            context.replyEmbeds(embed.build())
                .queue()

            musicManager.scheduler.player.stopTrack()
            musicManager.scheduler.queue.clear()
        }

    }

}