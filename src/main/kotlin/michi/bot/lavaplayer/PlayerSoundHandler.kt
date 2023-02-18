package michi.bot.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class PlayerSoundHandler(audioPlayer: AudioPlayer): AudioSendHandler {
    private val player: AudioPlayer                         // lavaplayer AudioPlayer
    private val buffer = ByteBuffer.allocate(1024) // bytes sent to discord every 20ms
    private val frame = MutableAudioFrame()                // where the bytes are being stored

    init {
        player = audioPlayer     // initializing the player
        frame.setBuffer(buffer) // sending the bytes to the buffer that after will provide to discord
    }

    // provides the frame to the player, so the player will write in the frame
    // and the frame content will be sent to discord
    override fun canProvide(): Boolean = player.provide(frame)

    override fun provide20MsAudio(): ByteBuffer? = buffer.flip()

    override fun isOpus(): Boolean = true

}
