package michi.bot.lavaplayer

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.util.concurrent.TimeUnit

import michi.bot.database.dao.GuildDAO
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object PlayerManager {

    private val musicManagers: MutableMap<Long, GuildMusicManager>
    private val playerManager: DefaultAudioPlayerManager
    const val SOUNDCLOUD_LOGO_URL = "https://developers.soundcloud.com/assets/logo_white-af5006050dd9cba09b0c48be04feac57.png"
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        musicManagers = HashMap()
        playerManager = DefaultAudioPlayerManager()

        val scManager = SoundCloudAudioSourceManager.createDefault()

        playerManager.registerSourceManager(scManager)
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    operator fun get(guild: Guild): GuildMusicManager {
        return musicManagers.computeIfAbsent(guild.idLong) { _ ->
            val musicManager = GuildMusicManager(playerManager, guild)
            guild.audioManager.sendingHandler = musicManager.sendHandler
            musicManager
        }
    }

    /**
     * Function that actually starts streaming the track. This function must be invoked when someone successfully calls the play command.
     * @param context The [SlashCommandInteractionEvent]
     * @param trackURL The string containing the URL or Name of the track
     * @author Slz
     */
    fun loadAndPlay(context: SlashCommandInteractionEvent, trackURL: String) = runBlocking {
        val guild = context.guild ?: return@runBlocking
        val musicManager = synchronized("") { PlayerManager[guild] }
        val botVoiceChannel = guild.selfMember.voiceState!!.channel?.asGuildMessageChannel()

        val err: YamlMap = getYML(context.user).yamlMap["error_messages"]!!
        val musicErr: YamlMap = err["music"]!!
        val success: YamlMap = getYML(guild).yamlMap["success_messages"]!!
        val musicSuccess: YamlMap = success["music"]!!
        val successEphemeral: YamlMap = getYML(guild).yamlMap["success_messages"]!!
        val musicSuccessEphemeral: YamlMap = successEphemeral["music"]!!
        val other: YamlMap = getYML(guild).yamlMap["other"]!!
        val otherEphemeral: YamlMap = getYML(context.user).yamlMap["other"]!!
        val musicOther: YamlMap = other.yamlMap["music"]!!
        val musicOtherEphemeral: YamlMap = otherEphemeral.yamlMap["music"]!!

        playerManager.loadItemOrdered(musicManager, trackURL, object : AudioLoadResultHandler {

            val queue = musicManager.scheduler.trackQueue

            override fun trackLoaded(track: AudioTrack?) {

                if (track == null) {
                    context.michiReply(String.format(musicErr.getText("unknown_error_finding_track"), Emoji.michiTired))
                    return
                }

                val trackTitle = track.info.title
                val trackAuthor = track.info.author
                val scheduler = musicManager.scheduler

                CoroutineScope(IO).launch {
                    GuildDAO.getMusicQueue(guild)?.plus("${track.info.uri},")?.let {
                        GuildDAO.setMusicQueue(guild, it)
                    }
                }

                scheduler.queue(track)

                val embed = EmbedBuilder()
                    .setColor(Color.MAGENTA)
                
                if (trackTitle == "「NIGHT RUNNING」") {
                    embed.setTitle("**$trackTitle**`[${formatTrackLength(track)}]` ${Emoji.nightRunning}", track.info.uri)
                    embed.setDescription("${String.format(musicOther.getText("uploaded_by"), trackAuthor)} | ${String.format(musicOther.getText("position"), queue.size)}")
                    embed.setImage(SOUNDCLOUD_LOGO_URL)
                }
                else {
                    embed.setTitle("**$trackTitle**`[${formatTrackLength(track)}]`", track.info.uri)
                    embed.setDescription("${String.format(musicOther.getText("uploaded_by"), trackAuthor)} | ${String.format(musicOther.getText("position"), queue.size)}")
                    embed.setImage(SOUNDCLOUD_LOGO_URL)
                }

                context.michiReply(embed.build(), message = musicSuccessEphemeral.getText("added_to_the_queue"))
                botVoiceChannel?.sendMessageEmbeds(embed.addField(String.format(musicOther.getText("requested_by")), context.user.asMention, false).build())
                    ?.queue()
                return
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
                if (playlist == null || playlist.tracks.isEmpty()) {
                    context.michiReply(String.format(musicErr.getText("unknown_error_finding_track"), Emoji.michiTired))
                    return
                }

                val embed = EmbedBuilder()
                    .setColor(Color.MAGENTA)

                if (playlist.isSearchResult) {
                    val firstTrack = playlist.tracks[0]
                    val trackTitle = firstTrack.info.title
                    val trackAuthor = firstTrack.info.author

                    musicManager.scheduler.queue(firstTrack)

                    CoroutineScope(IO).launch {
                        GuildDAO.getMusicQueue(guild)?.plus("${firstTrack.info.uri},")?.let {
                            GuildDAO.setMusicQueue(guild, it)
                        }
                    }

                    if (trackTitle == "「NIGHT RUNNING」") {
                        embed.setTitle("**$trackTitle**`[${formatTrackLength(firstTrack)}]` ${Emoji.nightRunning}", firstTrack.info.uri)
                        embed.setDescription("${String.format(musicOtherEphemeral.getText("uploaded_by"), trackAuthor)} | ${String.format(musicOther.getText("position"), queue.size)}")
                        embed.setImage(SOUNDCLOUD_LOGO_URL)
                    }
                    else {
                        embed.setTitle("**$trackTitle**`[${formatTrackLength(firstTrack)}]`", firstTrack.info.uri)
                        embed.setDescription("${String.format(musicOtherEphemeral.getText("uploaded_by"), trackAuthor)} | ${String.format(musicOther.getText("position"), queue.size)}")
                        embed.setImage(SOUNDCLOUD_LOGO_URL)
                    }

                    context.michiReply(embed.build(), message = musicSuccessEphemeral.getText("added_to_the_queue"))
                    botVoiceChannel?.sendMessageEmbeds(embed.addField(String.format(musicOther.getText("requested_by")), context.user.asMention, false).build())
                        ?.queue()
                    return
                }

                playlist.tracks.forEach { track ->
                    musicManager.scheduler.queue(track)
                    CoroutineScope(IO).launch {
                        GuildDAO.getMusicQueue(guild)
                            ?.plus("${track.info.uri},")
                            ?.let {
                                GuildDAO.setMusicQueue(guild, it)
                            }
                    }
                }

                val playlistAddedMessageEphemeral = musicSuccessEphemeral.getText("playlist_added_to_the_queue").split("\n")
                val playlistAddedMessage = musicSuccess.getText("playlist_added_to_the_queue").split("\n")

                val ephemeralEmbed = embed.apply {
                    setTitle(playlist.name)
                    setDescription(String.format(playlistAddedMessageEphemeral[1], playlist.tracks.size))
                    setImage(SOUNDCLOUD_LOGO_URL)
                }

                context.michiReply(ephemeralEmbed.build(), isEphemeral = true, message = playlistAddedMessageEphemeral[0])
                botVoiceChannel?.sendMessageEmbeds(
                    EmbedBuilder().apply {
                        setTitle(playlist.name)
                        setDescription(String.format(playlistAddedMessage[1], playlist.tracks.size))
                        setImage(SOUNDCLOUD_LOGO_URL)
                        addField(String.format(musicOther.getText("requested_by")), context.user.asMention, false)
                    }.build()
                )?.queue()
                return
            }

            override fun noMatches() =
                context.michiReply(String.format(musicErr.getText("couldnt_find_track"), Emoji.michiSad))

            override fun loadFailed(exception: FriendlyException?) =
                context.michiReply(String.format(musicErr.getText("couldnt_load_track"), Emoji.michiSad))
        })

    }

    private fun loadAndPlay(guild: Guild, trackUrl: String) {
        val musicManager = synchronized("") { this[guild] }

        playerManager.loadItemOrdered(musicManager, trackUrl, object : AudioLoadResultHandler {
            val scheduler = musicManager.scheduler

            override fun trackLoaded(track: AudioTrack?) {
                if (track == null) return
                scheduler.queue(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
                if (playlist == null || playlist.tracks.isEmpty()) return

                if (playlist.isSearchResult) {
                    playlist.tracks[0].let(musicManager.scheduler::queue)
                    return
                }
                playlist.tracks.forEach(musicManager.scheduler::queue)

            }

            override fun noMatches() {
                logger.error("One of the tracks of a guild playlist wasn't found.")
            }

            override fun loadFailed(exception: FriendlyException?) {
                logger.error("Something went wrong while loading a track\n${exception?.localizedMessage}")
            }
        })

    }

    suspend fun retrieveGuildMusicQueue(guild: Guild) = GuildDAO.getMusicQueue(guild)?.split(',')?.forEach { musicURI ->
        if (musicURI.isBlank()) return@forEach
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