package michi.bot.commands.util

import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

fun help(context: SlashCommandInteractionEvent) {
    val sender = context.user.asMention

    val embed = EmbedBuilder()
    embed.setColor(Color.MAGENTA)
        .setTitle("I'm here to help, $sender! ${Emoji.michiSmug}")
        .addField(
            "Commands Prefix: \"/\"",
            "All my commands are slashCommands, it means that if you type \"/\", you can see" +
                    "my commands, a description of what they do and what they need work.",
            false
        )
        .addField(
            "Terms Of Service:",
            "You need to follow some rules, so you can use my commands, at the moment, there aren't any rule, but if it" +
                    "changes, i'll let you know.",
            false
        )
    context.replyEmbeds(embed.build()).setEphemeral(true).queue()
}