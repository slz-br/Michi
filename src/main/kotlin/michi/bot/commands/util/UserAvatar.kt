package michi.bot.commands.util

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.awt.Color

@Suppress("Unused")
object UserAvatar: MichiCommand("user-avatar", "Sends you an image of the user you chose", CommandScope.GLOBAL_SCOPE) {

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/avatar <user>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("user", "The user to get the avatar", OptionType.USER, isRequired = true, hasAutoCompletion = false)
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user

        if (!canHandle(context)) return

        val user = context.getOption("user")?.asUser ?: return

        val embed = EmbedBuilder().apply {
            setColor(Color.WHITE)
            setDescription("${user.asMention}'s avatar")
            setImage(user.avatarUrl)
        }
        context.replyEmbeds(embed.build()).setEphemeral(true).queue()

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild

        guild?.let {
            val bot = it.selfMember

            if (!bot.permissions.containsAll(botPermissions)) {
                context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}").setEphemeral(true).queue()
                return false
            }

        }

        return true
    }

}