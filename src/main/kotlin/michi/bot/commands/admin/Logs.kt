package michi.bot.commands.admin

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.database.dao.GuildDao
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * Object for the "logs" command, a command that will set up a channel for logging messages.
 * @author Slz
 */
@Suppress("Unused")
object Logs: MichiCommand("logs", GUILD_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Enables/disables logs in the server",
            DiscordLocale.ENGLISH_UK to "Enables/disables logs in the server",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Habilita/disabilita o registro de eventos no servidor"
        )

    override val usage: String
        get() = "/$name <GuildTextChannel(optional - ignore this option if you want to disable the logs)>"

    override val ownerOnly = true

    override val arguments = listOf(
        MichiArgument(
            name = "channel",
            descriptionLocalization = mapOf(
                DiscordLocale.ENGLISH_US to "The channel to send the logs",
                DiscordLocale.ENGLISH_UK to "The channel to send the logs",
                DiscordLocale.PORTUGUESE_BRAZILIAN to "O canal para enviar os registros"
            ),
            type = OptionType.CHANNEL,
            isRequired = false
        )
    )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        val guild = context.guild!!

        val logChannel = context.getOption("channel")?.asChannel?.asGuildMessageChannel()

        GuildDao.setLogChannel(guild, logChannel)

        val success: YamlMap = getYML(context.user).yamlMap["success_messages"]!!
        val adminSuccess: YamlMap = success["admin"]!!

        if (logChannel == null) context.michiReply(String.format(adminSuccess.getText("logs_wont_be_notified_anymory"), Emoji.michiThumbsUp))
        else context.michiReply(String.format("logs_channel_setted", logChannel.asMention))
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild ?: return false
        val bot = guild.selfMember
        val logChannel = context.getOption("channel")?.asChannel
        val sender = context.member ?: return false

        val err: YamlMap = getYML(sender.user).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val adminErr: YamlMap = err["admin"]!!

        if (!sender.isOwner) {
            context.michiReply(genericErr.getText("user_isnt_owner"))
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        logChannel?.let {

            if (it !is GuildMessageChannelUnion) {
                context.michiReply(adminErr.getText("channel_type_err"))
                return false
            }

            if (!bot.hasAccess(logChannel) || !bot.hasPermission(logChannel)) {
                context.michiReply(String.format(genericErr.getText("bot_missing_channel_access"), Emoji.michiSad))
                return false
            }

        }

        return true
    }


}