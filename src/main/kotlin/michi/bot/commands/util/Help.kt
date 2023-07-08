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
object Help: MichiCommand("help", "Sends you a message containing helpful info", CommandScope.GLOBAL_SCOPE) {

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/help"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user

        val embed = EmbedBuilder()
        embed.setColor(Color.MAGENTA)
            .setTitle("I'm here to help, ${sender.name}! ${Emoji.michiSmug}")
            .addField(
                "Commands Prefix: \"**/**\"",
                "All my commands are slash commands, it means that if you type / you can see my commands, a description of what they do and what they need work.",
                false
            )
            .addField(
                "Terms Of Service:",
                "You need to follow some rules, so you can use my commands, at the moment, there aren't any rule, but if it changes, i'll let you know.",
                false
            )
        context.replyEmbeds(embed.build()).setEphemeral(true).queue()

        // puts the user that sent the command in cooldown
        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild

        guild?.let {
            val bot = guild.selfMember

            if (!bot.permissions.containsAll(botPermissions)) {
                context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}").setEphemeral(true).queue()
                return false
            }
        }

        return true
    }

}