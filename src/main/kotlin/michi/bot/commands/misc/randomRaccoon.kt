package michi.bot.commands.misc

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.io.BufferedReader
import java.io.FileReader
import kotlin.random.Random

fun randomRaccoon(context: SlashCommandInteractionEvent) {
    val raccoonImages = BufferedReader(FileReader(".\\txts\\raccoons.txt")).readLines()
    val imageUrl = raccoonImages[Random.nextInt(raccoonImages.size)]

    val embed = EmbedBuilder()
    embed.setColor(Color.MAGENTA)
        if (imageUrl.contains("gif")) { embed.setTitle("Raccoon Gif") }
        else { embed.setTitle("Raccoon Pic") }
        .setImage(imageUrl)
    context.replyEmbeds(embed.build())
        .setEphemeral(true)
        .queue()
}