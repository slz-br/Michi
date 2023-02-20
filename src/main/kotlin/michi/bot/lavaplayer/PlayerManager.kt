package michi.bot.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import kotlin.collections.HashMap
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import michi.bot.util.Emoji

/**
 * Gets the guild Music Manager and manages its player
 * @author Slz
 */

class PlayerManager {
    companion object {
        var instance: PlayerManager? = null
            get() {
                if (field == null) {
                    return PlayerManager()
                }
                return field
            }
            private set
    }

    private val playerManager: AudioPlayerManager

    // map that contains the musicManager of every server Michi is on
    private val musicManagers: MutableMap<Long, GuildMusicManager>

    init {

        playerManager = DefaultAudioPlayerManager()
        musicManagers = HashMap()

        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    fun getMusicManager(guild: Guild): GuildMusicManager {
        return musicManagers.computeIfAbsent(guild.idLong) {
            val newMusicManager = GuildMusicManager(playerManager)
            guild.audioManager.sendingHandler = newMusicManager.sendHandler

            newMusicManager
        }
    }

    fun loadAndPlay(context: SlashCommandInteractionEvent, trackUrl: String) {
        // gets the music manager from the guild of the command
        val musicManager = getMusicManager(context.guild!!)

        playerManager.loadItemOrdered(musicManager, trackUrl, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack?) {
                track ?: return
                musicManager.scheduler.updateQueue(track)

                val embed = EmbedBuilder()
                embed.setColor(Color.BLUE)
                    .setTitle("Added to the queue!")
                    .addField(track.info.title, "by: ${track.info.author}", false)
                    .setFooter("link: ${track.info.uri}")
                context.replyEmbeds(embed.build())
                    .queue()
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
                if (playlist == null) {
                    context.reply("Couldn't find any track ${Emoji.michiSad}")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                val embed = EmbedBuilder()
                val tracks = playlist.tracks

                if (playlist.isSearchResult) {
                    val track = tracks[0]
                    musicManager.scheduler.updateQueue(track)
                    embed.setColor(Color.BLUE)
                        .setTitle("Added to the queue!")
                        .addField(track.info.title, "by: ${track.info.author}", false)
                        .setFooter("link: ${track.info.uri}")
                    context.replyEmbeds(embed.build())
                        .queue()
                    return
                }

                embed.setColor(Color.BLUE)
                    .setTitle("Added to the queue!")
                    .addField(playlist.name, "${tracks.size} added to the queue", false)
                context.replyEmbeds(embed.build())
                    .queue()

                for (track in tracks) {
                    musicManager.scheduler.updateQueue(track)
                }
            }

            override fun noMatches() {
                context.reply("Couldn't find the track/playlist ${Emoji.smolMichiAngry}")
                    .setEphemeral(true)
                    .queue()
            }

            override fun loadFailed(track: FriendlyException?) {
                context.reply("Sorry! i couldn't play the track.")
                    .setEphemeral(true)
                    .queue()
            }

        })
    }

}