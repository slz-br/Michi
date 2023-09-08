package michi.bot.commands.misc

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import michi.bot.commands.CommandScope.GLOBAL_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.FileUpload
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

@Suppress("Unused")
object TypeRacer: MichiCommand("type-racer", GLOBAL_SCOPE) {
    val sessions = mutableMapOf<User, TypeRacerText>()
    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Sends you an image containing words - type them as fast as you can!",
            DiscordLocale.ENGLISH_UK to "Sends you an image containing words - type them as fast as you can!",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Te envia uma imagem que contem palavras - digite-as o mais rápido que puder!"
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        val user = context.user
        val other: YamlMap = getYML(user).yamlMap["other"]!!
        val miscOther: YamlMap = other["misc"]!!
        val wordsList = miscOther.getText("type_racer_words").split(" ")
        val words = wordsList.shuffled().slice(0 ..< 10).joinToString(" ")
        val bImage = BufferedImage((words.length * 35) + 20, 100, BufferedImage.TYPE_INT_ARGB)
        val g2d = bImage.createGraphics()

        g2d.apply {
            color = Color.BLACK
            fillRect(0, 0, bImage.width, bImage.height)
            color = Color.WHITE
            font = font.deriveFont(font.style, 75f)

            sessions[user] = TypeRacerText(words)
            drawString(words, 20f, (bImage.height/2)+5.5f)
            dispose()
        }

        val file = File("stopLookingAndType.png")
        withContext(Dispatchers.IO) {
            ImageIO.write(bImage, "png", file)
        }

        val fileToUpload = FileUpload.fromData(file)

        context.replyFiles(fileToUpload)
            .queue()
        delay(TimeUnit.SECONDS.toMillis(30))
        if (user in sessions && !sessions[user]!!.isAnswered && sessions[user]!!.timeElapsed >= 30000L) {
            context.channel.sendMessage("${user.asMention} took to long to type").queue { it.delete().queueAfter(10, TimeUnit.SECONDS)}
            sessions.remove(user)
        }
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val user = context.user

        val err: YamlMap = getYML(user).yamlMap["error_messages"]!!
        val miscErr: YamlMap = err["misc"]!!

        if (user in sessions && !sessions[user]!!.isAnswered) {
            context.michiReply(miscErr.getText("user_already_in_type_racer_session"))
            return false
        }

        return true
    }

    data class TypeRacerText(val text: String) {
        val startTime: Long = System.currentTimeMillis()
        val timeElapsed: Long
            get() = System.currentTimeMillis() - startTime
        var isAnswered: Boolean = false
    }

}