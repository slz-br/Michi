package michi.bot.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.coroutines.*
import michi.bot.database.dao.GuildsDAO
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.util.concurrent.TimeUnit

// todo: add soundcloud icon, since their api require it.

object PlayerManager {

    private val musicManagers: MutableMap<Long, GuildMusicManager>
    private val playerManager: DefaultAudioPlayerManager

    init {
        musicManagers = HashMap()
        playerManager = DefaultAudioPlayerManager()

        val scManager = SoundCloudAudioSourceManager.createDefault()

        playerManager.registerSourceManager(scManager)
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    fun getMusicManager(guild: Guild): GuildMusicManager {
        return musicManagers.computeIfAbsent(guild.idLong) { _ ->
            val musicManager = GuildMusicManager(playerManager, guild)
            guild.audioManager.sendingHandler = musicManager.sendHandler
            musicManager
        }
    }

    fun loadAndPlay(context: SlashCommandInteractionEvent, trackURL: String) {
        val guild = context.guild ?: return
        val musicManager = getMusicManager(guild)

        playerManager.loadItemOrdered(musicManager, trackURL, object : AudioLoadResultHandler {

            val queue = musicManager.scheduler.trackQueue

            override fun trackLoaded(track: AudioTrack?) {

                if (track == null) {
                    context.reply("Something went wrong ${Emoji.michiTired}\ntry typing the track name again, maybe in a different way")
                        .setEphemeral(true)
                        .queue()
                    return
                }

                val trackTitle = track.info.title
                val trackAuthor = track.info.author
                val scheduler = musicManager.scheduler

                CoroutineScope(Dispatchers.IO).launch {
                    val newQueue = GuildsDAO.selectMusicQueue(guild)?.plus("${track.info.uri},")?.removePrefix("null")
                    GuildsDAO.setMusicQueue(guild, newQueue)
                }

                val embed = EmbedBuilder()
                    .setColor(Color.MAGENTA)

                if (trackTitle == "「NIGHT RUNNING」") {
                    embed.addField(
                        "Added to the Queue!",
                        "**$trackTitle**`[${formatTrackLength(track)}]` ${Emoji.nightRunning}\nuploaded by: $trackAuthor | position: ${queue.size + 1}",
                        false
                    )
                    embed.setFooter("great choice!")
                }
                else {
                    embed.addField(
                        "Added to the Queue!",
                        "**$trackTitle**`[${formatTrackLength(track)}]`\nuploaded by: $trackAuthor | position: ${queue.size + 1}",
                        false
                    )
                }
                scheduler.queue(track)

                context.replyEmbeds(embed.build()).queue()
                return
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
                if (playlist == null || playlist.tracks.isEmpty()) {
                    context.reply("Something went wrong ${Emoji.michiTired}\ntry typing the track/playlist name again, maybe in a different way").setEphemeral(true).queue()
                    return
                }

                val embed = EmbedBuilder()
                    .setColor(Color.MAGENTA)

                if (playlist.isSearchResult) {
                    val firstTrack = playlist.tracks[0]
                    val trackTitle = firstTrack.info.title
                    val trackAuthor = firstTrack.info.author

                    musicManager.scheduler.queue(firstTrack)

                    CoroutineScope(Dispatchers.IO).launch {
                        val newQueue = GuildsDAO.selectMusicQueue(guild)?.plus("${firstTrack.info.uri},")?.removePrefix("null")
                        GuildsDAO.setMusicQueue(guild, newQueue)
                    }

                    if (trackTitle == "「NIGHT RUNNING」") {
                        embed.addField(
                            "Added to the Queue!",
                            "**$trackTitle**`[${formatTrackLength(firstTrack)}]` ${Emoji.nightRunning}\nuploaded by: $trackAuthor | position: ${queue.size + 1}",
                            false
                        )
                        embed.setFooter("Great choice!")
                        context.replyEmbeds(embed.build()).queue()
                        return
                    }
                    else {
                        embed.addField(
                            "Added to the Queue!",
                            "**$trackTitle**`[${formatTrackLength(firstTrack)}]`\nuploaded by: $trackAuthor | position: ${queue.size + 1}",
                            false
                        )
                        // todo: fix this.
                        // java.lang.IllegalStateException: This interaction has already been acknowledged or replied to. You can only reply or acknowledge an interaction once!
                        context.replyEmbeds(embed.build()).queue()
                        return
                    }

                }

                playlist.tracks.forEach { track ->
                    musicManager.scheduler.queue(track)
                    CoroutineScope(Dispatchers.IO).launch {
                        val newQueue = GuildsDAO.selectMusicQueue(guild)
                            ?.plus("${track.info.uri},")
                            ?.removePrefix("null")
                        GuildsDAO.setMusicQueue(guild, newQueue)
                    }
                }

                embed.setTitle("Playlist added!")
                    .addField(
                        playlist.name,
                        "${playlist.tracks.size} tracks added to the queue",
                        false
                    )

                context.replyEmbeds(embed.build())
                    .queue()
                return
            }

            override fun noMatches() =
                context.reply("Couldn't find any track ${Emoji.michiSad}")
                    .setEphemeral(true)
                    .queue()


            override fun loadFailed(exception: FriendlyException?) =
                context.reply("Sorry, I couldn't load the track ${Emoji.michiSad}")
                    .setEphemeral(true)
                    .queue()
        })

    }

    private fun loadAndPlay(guild: Guild, trackURL: String) {

        playerManager.loadItemOrdered(getMusicManager(guild), trackURL, object : AudioLoadResultHandler {
            val scheduler = getMusicManager(guild).scheduler

            override fun trackLoaded(track: AudioTrack?) {

                if (track == null) return

                CoroutineScope(Dispatchers.IO).launch {
                    val newQueue = GuildsDAO.selectMusicQueue(guild)?.plus("${track.info.uri},")?.removePrefix("null")
                    GuildsDAO.setMusicQueue(guild, newQueue)
                }

                scheduler.queue(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
                if (playlist == null || playlist.tracks.isEmpty()) return

                if (playlist.isSearchResult) {
                    val firstTrack = playlist.tracks[0]

                    getMusicManager(guild).scheduler.queue(firstTrack)

                    CoroutineScope(Dispatchers.IO).launch {
                        val newQueue = GuildsDAO.selectMusicQueue(guild)?.plus("${firstTrack.info.uri},")?.removePrefix("null")
                        GuildsDAO.setMusicQueue(guild, newQueue)
                    }

                }

                CoroutineScope(Dispatchers.IO).launch {
                    val newQueue = GuildsDAO.selectMusicQueue(guild)?.plus("$trackURL,")?.removePrefix("null")
                    GuildsDAO.setMusicQueue(guild, newQueue)
                }

                playlist.tracks.forEach { track ->
                    getMusicManager(guild).scheduler.queue(track)
                }

            }

            override fun noMatches() = Unit

            override fun loadFailed(exception: FriendlyException?) = Unit
        })

    }

    suspend fun retrieveGuildMusicQueue(guild: Guild) = GuildsDAO.selectMusicQueue(guild)
        ?.split(',')?.forEach { musicURI ->
        loadAndPlay(guild, musicURI)
    }

    private fun formatTrackLength(track: AudioTrack): String {
        val timeInMiliss = track.duration

        val hours = timeInMiliss / TimeUnit.HOURS.toMillis(1)
        val minutes = timeInMiliss / TimeUnit.MINUTES.toMillis(1)
        val seconds = timeInMiliss % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1)

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

}