package michi.bot.commands.misc

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import michi.bot.commands.CommandScope
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.io.BufferedReader
import java.io.FileReader
import kotlin.random.Random

/**
 * Object for the "raccoon" command, a command that sends you a random image or gif of a raccoon.
 * @author Slz
 * @see execute
 */
object Raccoon: MichiCommand("raccoon", "Sends you a random raccoon pic or gif", CommandScope.GUILD_SCOPE) {

    override val usage: String
        get() = "/raccoon"

    override val botPermisions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_EMBED_LINKS
        )

    /**
     * Sends a random picture or gif of a raccoon.
     * @param context The interaction to reply to.
     * @author Slz
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val raccoonImages = BufferedReader(FileReader(".\\txts\\raccoons.txt")).readLines()
        val imageUrl = raccoonImages[Random.nextInt(raccoonImages.size)]
        val guild = context.guild

        if (!canHandle(context)) return
        if (guild != null) {
            val bot = guild.selfMember
            if (!bot.permissions.any { permission -> botPermisions.contains(permission) }) {
                context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}").setEphemeral(true).queue()
                return
            }
        }

        val embed = EmbedBuilder()
        embed.setColor(Color.MAGENTA)
        if (imageUrl.contains("gif")) {
            embed.setTitle("Raccoon Gif")
        } else {
            embed.setTitle("Raccoon Pic")
        }
            .setImage(imageUrl)
        context.replyEmbeds(embed.build())
            .setEphemeral(true)
            .queue()

        // puts the user that sent the command in cooldown
        GlobalScope.launch { SlashCommandListener.cooldownManager(sender) }
    }

    override fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild
        if (guild != null) {
            val bot = guild.selfMember
            if (!bot.permissions.any { permission -> botPermisions.contains(permission) }) {
                context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}").setEphemeral(true).queue()
                return false
            }
        }

        return true
    }
}