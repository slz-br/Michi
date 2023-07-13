package michi.bot.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild

class GuildMusicManager(manager: AudioPlayerManager, guild: Guild) {

    val player: AudioPlayer = manager.createPlayer()
    val scheduler = Scheduler(player, guild)
    val sendHandler = PlayerSendHandler(player)
    var playingTrack: AudioTrack? = null
        get() = player.playingTrack
        private set

    init {
        player.addListener(scheduler)
    }

}