package michi.bot.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

/**
 * Class where the I/O shit happens.
 */
class PlayerSendHandler(private val player: AudioPlayer): AudioSendHandler {

    private val byteBuffer = ByteBuffer.allocate(1024)
    private val frame = MutableAudioFrame()

    init {
        frame.setBuffer(byteBuffer)
    }

    override fun canProvide() = player.provide(frame)

    override fun isOpus() = true

    override fun provide20MsAudio(): ByteBuffer? = byteBuffer.flip()

}