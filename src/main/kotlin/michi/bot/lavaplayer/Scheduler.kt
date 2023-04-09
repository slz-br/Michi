package michi.bot.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import java.util.concurrent.LinkedBlockingQueue

class Scheduler(player: AudioPlayer): AudioEventAdapter() {

    val trackQueue = LinkedBlockingQueue<AudioTrack>()
    private val audioPlayer = player

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) = if (endReason!!.mayStartNext) nextTrack() else Unit

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {
        super.onTrackStart(player, track)
    }

    fun queue(track: AudioTrack) = if (!audioPlayer.startTrack(track, true)) trackQueue.offer(track) else false

    private fun nextTrack() {
        audioPlayer.startTrack(trackQueue.poll(), false)
    }

}