package michi.bot.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.util.concurrent.TimeUnit
object PlayerManager {

    private val musicManagers: MutableMap<Long, GuildMusicManager>
    private val audioPlayerManager: DefaultAudioPlayerManager

    init {
        musicManagers = HashMap()
        audioPlayerManager = DefaultAudioPlayerManager()

        AudioSourceManagers.registerRemoteSources(audioPlayerManager)
        AudioSourceManagers.registerLocalSource(audioPlayerManager)
    }

    fun getMusicManager(guild: Guild): GuildMusicManager {
        return musicManagers.computeIfAbsent(guild.idLong) { _ ->
            val musicManager = GuildMusicManager(audioPlayerManager)
            guild.audioManager.sendingHandler = musicManager.sendHandler

            musicManager
        }
    }

    fun loadAndPlay(context: SlashCommandInteractionEvent, trackURL: String) {
        val guild = context.guild!!
        val musicManager = getMusicManager(guild)

        audioPlayerManager.loadItemOrdered(musicManager, trackURL, object : AudioLoadResultHandler {

            val queue = musicManager.scheduler.trackQueue

            override fun trackLoaded(track: AudioTrack?) {

                if (track == null) {
                    context.reply("Something went wrong ${Emoji.smolMichiAngry}\ntry typing the track name again, maybe in a different way").setEphemeral(true).queue()
                    return
                }

                val trackTitle = track.info.title
                val trackAuthor = track.info.author
                val scheduler = musicManager.scheduler

                scheduler.queue(track)

                val embed = EmbedBuilder()
                    .setColor(Color.MAGENTA)

                    if (trackTitle == "Night Running" && trackAuthor == "Shin Sakiura") {
                        embed.addField(
                            "Added to the Queue!",
                            "**$trackTitle**`[${formatTrackLength(track)}]` ${Emoji.nightRunning}\nauthor: $trackAuthor | position: ${queue.size + 1}",
                            false
                        )
                        embed.setFooter("great choice!")
                    }

                    embed.addField(
                        "Added to the Queue!",
                        "**$trackTitle**`[${formatTrackLength(track)}]`\nauthor: $trackAuthor | position: ${queue.size + 1}",
                        false
                    )

                context.replyEmbeds(embed.build()).queue()
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
                if (playlist == null) {
                    context.reply("Something went wrong ${Emoji.smolMichiAngry}\ntry typing the track/playlist name again, maybe in a different way").setEphemeral(true).queue()
                    return
                }

                val embed = EmbedBuilder()
                    .setColor(Color.MAGENTA)

                if (playlist.isSearchResult) {
                    val firstTrack = playlist.tracks[0]
                    val trackTitle = firstTrack.info.title
                    val trackAuthor = firstTrack.info.author

                    musicManager.scheduler.queue(firstTrack)

                    embed.addField(
                        "Added to the Queue!",
                        "**$trackTitle**`[${formatTrackLength(firstTrack)}]`\nauthor: $trackAuthor | position: ${queue.size + 1}",
                        false
                    )

                }

                playlist.tracks.forEach { track ->
                    musicManager.scheduler.queue(track)
                }

                embed.setTitle("Playlist added!")
                    .addField(
                        playlist.name,
                        "${playlist.tracks.size} tracks added to the queue",
                        false
                    )

                context.replyEmbeds(embed.build()).queue()
            }

            override fun noMatches() =
                context.reply("Couldn't find any track ${Emoji.michiSad}").setEphemeral(true).queue()

            override fun loadFailed(exception: FriendlyException?) =
                context.reply("Sorry, I couldn't load the track ${Emoji.michiSad}").setEphemeral(true).queue()
        })

    }

    private fun formatTrackLength(track: AudioTrack): String {
        val timeInMiliss = track.duration

        val hours = timeInMiliss / TimeUnit.HOURS.toMillis(1)
        val minutes = timeInMiliss / TimeUnit.MINUTES.toMillis(1)
        val seconds = timeInMiliss % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1)

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

}