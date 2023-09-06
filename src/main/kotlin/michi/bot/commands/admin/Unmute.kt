package michi.bot.commands.admin

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * Object for the unmute command, a command that removes the timeout of a user
 * if possible(the user to be unmuted is currently muted and the sender of the command
 * has permission to unmute the user)
 * @author Slz
 */
@Suppress("Unused")
object Unmute: MichiCommand("unmute", GUILD_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Unmute a user if the user is muted",
            DiscordLocale.ENGLISH_UK to "Unmute a user if the user is muted",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Desilencia um usuário se ele estiver silenciado"
        )

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
            MichiArgument("user", OptionType.USER)
        )

    override val usage: String
        get() = "/$name <user>"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return

        val memberToMute = context.getOption("user")!!.asMember!!
        memberToMute.removeTimeout().queue()

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val adminSuccess: YamlMap = success["admin"]!!

        context.michiReply(String.format(adminSuccess.getText("unmute_applied"), memberToMute.asMention, Emoji.michiThumbsUp), false)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val memberToUnmute = context.getOption("user")?.asMember
        val guild = context.guild ?: return false
        val bot = guild.selfMember

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val adminErr: YamlMap = err["admin"]!!

        if (memberToUnmute == null) {
            context.michiReply(String.format(genericErr.getText("user_not_found"), Emoji.michiShrug))
            return false
        }

        if (!sender.permissions.any(userPermissions::contains)) {
            context.michiReply(String.format(genericErr.getText("user_missing_perms"), Emoji.michiBlep))
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        if (!memberToUnmute.isTimedOut) {
            context.michiReply(String.format(adminErr.getText("user_not_muted"), memberToUnmute.asMention))
            return false
        }

        return true
    }

}