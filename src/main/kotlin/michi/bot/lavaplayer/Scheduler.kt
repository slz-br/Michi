package michi.bot.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import java.util.concurrent.LinkedBlockingQueue

class Scheduler(audioPlayer: AudioPlayer): AudioEventAdapter() {

    val queue = LinkedBlockingQueue<AudioTrack>()
    val player: AudioPlayer

    init {
        player = audioPlayer
    }

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) {
        endReason ?: return
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }

    fun nextTrack() {
        player.startTrack(queue.poll(), false)
    }

    fun updateQueue(track: AudioTrack) {
        if (!player.startTrack(track, true)) {
            queue.offer(track)
        }
    }

}