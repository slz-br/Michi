package michi.bot.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import net.dv8tion.jda.api.audio.AudioSendHandler

class GuildMusicManager(manager: AudioPlayerManager) {

    private val player: AudioPlayer
    val scheduler: Scheduler
    val sendHandler: AudioSendHandler

    init {
        player = manager.createPlayer()
        scheduler = Scheduler(player)
        player.addListener(scheduler)
        sendHandler = PlayerSoundHandler(player)
    }

}