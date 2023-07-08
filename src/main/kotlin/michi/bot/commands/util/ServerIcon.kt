package michi.bot.commands.util

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

@Suppress("Unused")
object ServerIcon: MichiCommand("server-icon", "Gives you an image containing the icon of the server", CommandScope.GUILD_SCOPE) {

    override val usage: String
        get() = "/server-icon"

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val guild = context.guild ?: return

        if (!canHandle(context)) return

        if (guild.icon == null) {
            context.reply("This server doesn't have an icon ${Emoji.michiShrug}").setEphemeral(true).queue()
            return
        }

        val embed = EmbedBuilder().apply {
            setColor(Color.WHITE)
            setImage(guild.iconUrl)
            setDescription("${guild.name}'s Icon")
        }

        context.replyEmbeds(embed.build()).setEphemeral(true).queue()

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild ?: return false
        val bot = guild.selfMember

        if (!bot.permissions.containsAll(botPermissions)) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}").setEphemeral(true).queue()
            return false
        }

        return true
    }

}