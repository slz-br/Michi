package michi.bot.commands.admin

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.database.dao.GuildsDAO
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * Object for the "logs" command, a command that will set up a channel for logging messages.
 * @author Slz
 */
@Suppress("Unused")
object Logs: MichiCommand("logs", "Enables/disables logs in the server", CommandScope.GUILD_SCOPE) {
    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_SEND_IN_THREADS,
            Permission.MESSAGE_EMBED_LINKS
        )

    override val usage: String
        get() = "/logs <GuildTextChannel(optional - ignore this option if you want to disable the logs)>"

    override val ownerOnly: Boolean
        get() = true

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument(
                name = "channel",
                description = "The channel for posting logs",
                type = OptionType.CHANNEL,
                isRequired = false,
            )
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val guild = context.guild!!

        if (!canHandle(context)) return

        val logChannel = context.getOption("channel")?.asChannel?.asGuildMessageChannel()

        GuildsDAO.setLogChannel(guild, logChannel)

        if (logChannel == null)
            context.reply("Done! log messages will no longer be notified ${Emoji.michiThumbsUp}").setEphemeral(true).queue()
        else
            context.reply("Done! log messages will be sent in ${logChannel.asMention}").setEphemeral(true).queue()

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild ?: return false
        val bot = guild.selfMember
        val logChannel = context.getOption("channel")?.asChannel
        val sender = context.member ?: return false

        if (!sender.isOwner) {
            context.reply("Only the server owner can use this command.")
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

        logChannel?.let {

            if (it !is GuildMessageChannelUnion) {
                context.reply("Invalid channel, the channel must be a guild text channel")
                    .setEphemeral(true)
                    .queue()
                return false
            }

            if (!bot.hasAccess(logChannel) || !bot.hasPermission(logChannel)) {
                context.reply("I don't have permission to send messages in that channel ${Emoji.michiSad}")
                    .setEphemeral(true)
                    .queue()
                return false
            }

        }

        return true
    }


}