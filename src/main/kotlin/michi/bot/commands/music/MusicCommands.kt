package michi.bot.commands.music

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

abstract class MusicCommands: AudioEventAdapter() {

    companion object {

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

    }

}