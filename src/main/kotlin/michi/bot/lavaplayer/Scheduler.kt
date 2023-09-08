package michi.bot.lavaplayer

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.api.EmbedBuilder
import org.slf4j.LoggerFactory
import net.dv8tion.jda.api.entities.Guild
import java.awt.Color
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

import michi.bot.commands.music.guildSkipPoll
import michi.bot.database.dao.GuildDAO
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML

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

    /**
     * If true, the current [AudioTrack] playing will replay when it ends.
     */
    var isLooping = false

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) {

        guildSkipPoll[schedulerGuild]?.clear()
        val voiceChannel = schedulerGuild.selfMember.voiceState?.channel
        val membersInVC = voiceChannel?.members?.filter { !it.user.isBot }

        if (membersInVC != null && membersInVC.isEmpty()) {
            schedulerGuild.audioManager.closeAudioConnection()
            return
        }

        if (trackQueue.isEmpty() && player?.playingTrack == null) {
            CoroutineScope(IO).launch {
                delay(TimeUnit.MINUTES.toMillis(3))
                if (trackQueue.isEmpty() && player?.playingTrack == null) {
                    schedulerGuild.audioManager.closeAudioConnection()
                    return@launch
                }
            }
        }

        if (isLooping) {
            player?.startTrack(track?.makeClone(), true)
            return
        }

        if (endReason!!.mayStartNext) nextTrack()

        CoroutineScope(IO).launch {
            track?.info?.uri?.let {
                GuildDAO.getMusicQueue(schedulerGuild)?.replace("$it,", "")?.let { newTrackQueue ->
                    GuildDAO.setMusicQueue(schedulerGuild, newTrackQueue)
                }
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
        trackQueue.remove(track)
        audioPlayer.startTrack(track, false)

        return true
    }

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exc: FriendlyException?) =
        LoggerFactory.getLogger(this::class.java).warn(exc?.message)

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {
        val channel = schedulerGuild.selfMember.voiceState?.channel?.asGuildMessageChannel()
        val bot = schedulerGuild.selfMember

        if (channel == null
            || !bot.hasPermission(channel)
            || track == null
        ) return

        if (isLooping) return

        val other: YamlMap = runBlocking { getYML(schedulerGuild).yamlMap["other"]!! }
        val musicOther: YamlMap = other["music"]!!

        EmbedBuilder().apply {
            setColor(Color.MAGENTA)
            setTitle(musicOther.getText("scheduler_now_playing"), track.info.uri)
            addField(
                track.info.title,
                String.format(musicOther.getText("coming_next"), trackQueue.firstOrNull()?.info?.title ?: String.format(musicOther.getText("nothing_coming_next"), Emoji.michiSaddened)),
                false
            )
        }.build().let(channel::sendMessageEmbeds).queue()
    }

}