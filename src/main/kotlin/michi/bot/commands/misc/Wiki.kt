package michi.bot.commands.misc

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import michi.bot.commands.CommandScope
import michi.bot.commands.MichiCommand
import michi.bot.commands.admin.Ban
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.jsoup.Jsoup
import java.awt.Color

/**
 * Object for the "wiki" command, a command that gives you a random wikipedia article.
 * @author Slz
 * @see execute
 */
object Wiki: MichiCommand("wiki", "Gives you a random wikipedia article.", CommandScope.GUILD_SCOPE) {
    override val usage: String
        get() = "/wiki"

    override val botPermisions: List<Permission>
        get() = listOf(Permission.MESSAGE_SEND)

    /**
     * Searches a random wikipedia article and sends it to the user
     * @param context The interaction to reply to.
     * @author Slz
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val guild = context.guild

        if (!canHandle(context)) return

        if (guild != null) {
            val bot = guild.selfMember
            if (!bot.permissions.any { permission -> Ban.botPermisions.contains(permission) }) {
                context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}").setEphemeral(true).queue()
                return
            }
        }

        val article = Jsoup.connect("https://en.wikipedia.org//wiki/Special:Random").get()
        val articleTitle = article.getElementsByTag("h1").first()?.text()
        val articleParagraphs = article.getElementsByTag("p")
        val articleUri = article.baseUri()
        var firstParagraph = "Couldn't load the content ${Emoji.michiSad}"

        for (paragraph in articleParagraphs) {
            if (paragraph.getElementsByTag("b").first() != null) {
                firstParagraph = paragraph.text()
                break
            }
        }

        val embed = EmbedBuilder()
        embed.setColor(Color.WHITE)
            .setTitle("Wiki", articleUri)
            .addField(articleTitle ?: "Title not found ${Emoji.michiTired}", firstParagraph, false)
            .setFooter("Read more at: $articleUri")
        context.replyEmbeds(embed.build())
            .setEphemeral(true)
            .queue()

        // puts the user that sent the command in cooldown
        GlobalScope.launch { SlashCommandListener.cooldownManager(sender) }
    }

    override fun canHandle(context: SlashCommandInteractionEvent): Boolean = true
}