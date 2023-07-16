package michi.bot.commands.admin

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

@Suppress("Unused")
object Unmute: MichiCommand("unmute", "Unmutes a user if the user is muted.", CommandScope.GUILD_SCOPE) {

    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR,
            Permission.MODERATE_MEMBERS
        )

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MODERATE_MEMBERS,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("user", "The user to unmute.", OptionType.USER)
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val channel = context.channel

        if (!canHandle(context)) return

        val memberToMute = context.getOption("user")!!.asMember!!
        memberToMute.removeTimeout().queue()

        context.reply("Successfully unmuted ${memberToMute.asMention} ${Emoji.michiThumbsUp}").setEphemeral(true).queue()
        channel.sendMessage("${sender.asMention} unmuted ${memberToMute.asMention}").queue()

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val memberToUnmute = context.getOption("user")?.asMember
        val guild = context.guild ?: return false
        val bot = guild.selfMember

        if (memberToUnmute == null) {
            context.reply("Couldn't find the user in the server ${Emoji.michiThink}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!sender.permissions.any { permission -> userPermissions.contains(permission) } ) {
            context.reply("You don't have the permissions to use this command, silly you ${Emoji.michiBlep}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!memberToUnmute.isTimedOut) {
            context.reply("${memberToUnmute.asMention} isn't muted.")
                .setEphemeral(true)
                .queue()
            return false
        }

        return true
    }

}