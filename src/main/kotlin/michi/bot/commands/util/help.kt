package michi.bot.commands.util

import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

fun help(context: SlashCommandInteractionEvent) {
    val embed = EmbedBuilder()
    embed.setColor(Color.MAGENTA)
        .setTitle("I'm here to help!")
        .addField("About Michi:", "Hey! I'm Michi, i'm here to bring some fun to this dreary place ${Emoji.michiSmug}\n" +
                "I can do lots of things and I'm constantly improving to do even more cool things. Some things that i can do is" +
                "play music, ban bad people and be awesome as always.", false)
    context.replyEmbeds(embed.build())
        .setEphemeral(true)
        .queue()
}