package michi.bot.commands.misc

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GLOBAL_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.awt.Color

private val raccoonImages = listOf(
    "https://curiodyssey.org/wp-content/uploads/2017/03/Mammals-Raccoon.jpg",
    "https://busites-www.s3.amazonaws.com/blog-margaritaville/2016/04/Curious_Raccoon.jpgCurious_Raccoon-694x533.jpg",
    "https://www.ndow.org/wp-content/uploads/2021/10/Family-of-Raccoons.jpg",
    "https://www.vmcdn.ca/f/files/shared/stock-images/racoon.jpg;w=960",
    "https://mdc.mo.gov/sites/default/files/styles/gallery_main_image/public/2020-04/raccoon_0.jpg?itok=iMYt00bJ",
    "https://fpdcc.com/wp-content/uploads/2018/12/miami-woods-raccoon-frank-hildebrand-oct15.jpg",
    "https://www.wkbn.com/wp-content/uploads/sites/48/2022/06/raccoon.jpg",
    "https://i.pinimg.com/originals/8b/22/de/8b22de0690b179400541988a6d8c6de5.jpg",
    "https://global.discourse-cdn.com/business4/uploads/ine/original/1X/b469f602101c113a109a0afe7d11470c1cd042a0.jpeg",
    "https://w0.peakpx.com/wallpaper/396/980/HD-wallpaper-racoons-cup-animal-animals-cute-raccoon-wholesome.jpg",
    "https://townsquare.media/site/701/files/2020/06/racoon.jpg?w=980&q=75",
    "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8d/Raccoon_Cute_Pose_%28cropped%29.jpg/988px-Raccoon_Cute_Pose_%28cropped%29.jpg",
    "https://www.indy100.com/media-library/image.jpg?id=28061433&width=1245&height=700&quality=85&coordinates=0%2C54%2C0%2C54",
    "https://www.rd.com/wp-content/uploads/2021/04/GettyImages-1165332891-scaled.jpg",
    "https://www.rd.com/wp-content/uploads/2021/04/GettyImages-86146566.jpg",
    "https://www.rd.com/wp-content/uploads/2021/04/GettyImages-1165311356.jpg",
    "https://www.rd.com/wp-content/uploads/2021/04/GettyImages-670948423.jpg",
    "https://www.rd.com/wp-content/uploads/2021/04/GettyImages-91702654-scaled-e1617285570216.jpg",
    "https://www.rd.com/wp-content/uploads/2021/04/GettyImages-1141990926-scaled-e1617287596105.jpg",
    "https://media.tenor.com/CDNYr8w0P6kAAAAM/raccoon-retreat.gif",
    "https://media.tenor.com/TS0cA2Y7izgAAAAM/raccoon-racon.gif",
    "https://media.tenor.com/BZe0e5t0TvcAAAAM/raccoon-playing-piano-focused.gif",
    "https://media.tenor.com/XQTCRnQ1aooAAAAM/raccoon-ayasan.gif"
)

/**
 * Object for the "raccoon" command, a command that sends you a random image or gif of a raccoon.
 * @author Slz
 * @see execute
 */
@Suppress("Unused")
object Raccoon: MichiCommand("raccoon", GLOBAL_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Sends you a random raccoon pic or gif",
            DiscordLocale.ENGLISH_UK to "Sends you a random raccoon pic or gif",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Te envia um gif ou foto aleatória de um guaxinim"
        )

    /**
     * Sends a random picture or gif of a raccoon.
     * @param context The interaction to reply to.
     * @author Slz
     * @see canHandle
     */
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val imageUrl = raccoonImages.random()

        if (!canHandle(context)) return

        val raccoonMediaType = getYML(context.user).yamlMap.get<YamlMap>("success_messages")!!.get<YamlMap>("misc")!!
            .getText("raccoon_media")
            .split("\n") // I know, this looks confusing, but this is just a list with the elements "Raccoon Pic" or "Raccoon GIF"
                                  //  I couldn't think of a better name for the variable
        // building the embed message
        val embed = EmbedBuilder()
        embed.setColor(Color.MAGENTA)

        if (imageUrl.contains("gif")) embed.setTitle(raccoonMediaType[0])
        else embed.setTitle(raccoonMediaType[1])

        embed.setImage(imageUrl)

        // sending the embed message
        context.michiReply(embed.build())
    }

    /**
     * Checks if the command comes from a guild, if so, check if the bot has permission to execute the command
     * @param context The interaction to check and reply to.
     * @return true if it's possible to handle the command, false otherwise.
     * @author Slz
     * @see execute
     */
    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        context.guild?.let { guild ->
            val bot = guild.selfMember

            val err: YamlMap = getYML(context.user).yamlMap["error_messages"]!!
            val genericErr: YamlMap = err["generic"]!!

            if (!bot.permissions.containsAll(botPermissions)) {
                context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
                return false
            }
        }
        return true
    }
}