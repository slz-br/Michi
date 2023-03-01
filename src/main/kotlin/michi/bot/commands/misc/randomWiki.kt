package michi.bot.commands.misc

import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.jsoup.Jsoup
import java.awt.Color

/**
 * Searchs a random wikipedia article and sends it to the user
 * @param context The SlashCommandInteractionEvent that called this function.
 * @author Slz
 */

fun randomWiki(context: SlashCommandInteractionEvent) {
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
}