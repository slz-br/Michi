package michi.bot.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

import michi.bot.commands.music.guildSkipPoll
import michi.bot.database.dao.GuildsDAO
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import org.slf4j.LoggerFactory
import java.awt.Color

class Scheduler(player: AudioPlayer, guild: Guild): AudioEventAdapter() {

    /**
     * The queue of [AudioTrack] containing all tracks of the guild.
     */
    val trackQueue = LinkedBlockingQueue<AudioTrack>()

    /**
     * The [AudioPlayer] instance of this [Scheduler].
     */
    private val audioPlayer = player

    /**
     * The [Guild] of this [Scheduler].
     */
    private val schedulerGuild = guild

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) {

        guildSkipPoll[schedulerGuild]?.clear()

        if (trackQueue.isEmpty() && player?.playingTrack == null) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(TimeUnit.MINUTES.toMillis(3))
                if (trackQueue.isEmpty() && player?.playingTrack == null) {
                    schedulerGuild.audioManager.closeAudioConnection()
                }
            }
        }

        if (endReason!!.mayStartNext) nextTrack()
        CoroutineScope(Dispatchers.IO).launch {
            track?.info?.uri?.let {
                val newQueue = GuildsDAO.getMusicQueue(schedulerGuild)?.replace("$it,", "")
                GuildsDAO.setMusicQueue(schedulerGuild, newQueue)
            }
        }
    }

    /**
     * Tries to start the track, if it isn't possible, the track is added to the queue.
     * @param track The track to play or add to the queue.
     * @author Slz
     */
    fun queue(track: AudioTrack) {
        if (!audioPlayer.startTrack(track, true)) trackQueue.offer(track)
    }

    /**
     * Starts the next track ignoring if a track is already playing.
     * @author Slz
     */
    fun nextTrack() {
        val newTrack = trackQueue.poll()
        audioPlayer.startTrack(newTrack, false)
        val voiceChannel = schedulerGuild.selfMember.voiceState?.channel?.asVoiceChannel()
        val bot = schedulerGuild.selfMember

        if (voiceChannel == null || !bot.hasPermission(voiceChannel) || newTrack == null) return
        EmbedBuilder().apply {
            setColor(Color.MAGENTA)
            setTitle("Now Playing:", newTrack.info.uri)
            addField(
                newTrack.info.title,
                "coming next: ${trackQueue.firstOrNull()?.info?.title ?: "No more music, the queue ended ${Emoji.michiSaddened}"}",
                false
            )
        }.build().let(voiceChannel::sendMessageEmbeds).queue()
    }

    /**
     * Starts the track in the specified index ignoring if a track is already playing.
     * @param index The index of the track
     * @return true if the index isn't negative and doesn't exceed the trackQueue size, false otherwise.
     * @author Slz
     */
    fun playTrackAt(index: Int): Boolean {
        if ((index - 1) < 0 || index > trackQueue.size) return false

        val track = trackQueue.elementAt(index - 1)
        audioPlayer.startTrack(track, false)
        val voiceChannel = schedulerGuild.selfMember.voiceState?.channel?.asVoiceChannel()
        val bot = schedulerGuild.selfMember

        if (voiceChannel == null || !bot.hasPermission(voiceChannel)) return true
        EmbedBuilder().apply {
            setColor(Color.MAGENTA)
            setTitle("Now Playing:", track.info.uri)
            addField(
                track.info.title,
                "coming next: ${trackQueue.firstOrNull()?.info?.title ?: "No more music, the queue ended ${Emoji.michiSaddened}"}",
                false
            )
        }.build().let(voiceChannel::sendMessageEmbeds).queue()

        return true
    }

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException?) {
        LoggerFactory.getLogger(this::class.java)
    }

}