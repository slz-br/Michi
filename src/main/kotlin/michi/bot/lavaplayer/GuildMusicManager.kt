package michi.bot.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager

class GuildMusicManager(manager: AudioPlayerManager) {

    private val player = manager.createPlayer()
    val scheduler = Scheduler(player)
    val sendHandler = PlayerSendHandler(player)
    init {
        player.addListener(scheduler)
    }

}