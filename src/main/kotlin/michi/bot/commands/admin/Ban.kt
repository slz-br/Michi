package michi.bot.commands.admin

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.config
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button

/**
 * Object for the "ban" command, a command that bans the mentioned user from
 * the guild if possible(case the sender and the bot have permission to ban the user)
 * @author Slz
 */
@Suppress("Unused")
object Ban: MichiCommand("ban", GUILD_SCOPE) {

    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR,
            Permission.BAN_MEMBERS
        )

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.BAN_MEMBERS,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/$name <user> <reason(optional)>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("user", OptionType.USER),
            MichiArgument("reason", OptionType.STRING, isRequired = false)
        )

    /**
     * Searches for a member in the guild.
     * @param guild The guild to look for the user;
     * @param user The user to search on the guild.
     * @return True if the user was found in the guild, false if not.
     * @author Slz
     */
    private fun locateUserInGuild(guild: Guild, user: User): Boolean {
        var userNotFound = false

        guild.members.any { it.user == user }
        guild.retrieveMember(user).queue(null) { userNotFound = true }
        return userNotFound
    }

    /**
     * Bans the mentioned member(s) if possible
     * @param context The SlashCommandInteractionEvent that called this function
     * @author Slz
     * @see canHandle
     */
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return

        val subject = context.getOption("user")!!.asMember!!
        var reason = context.getOption("reason")?.asString

        if (reason != null && reason.length > 1750) reason = null

        val errMsg: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val adminErr: YamlMap = errMsg["admin"]!!
        val warnMsg: YamlMap = getYML(context).yamlMap["warn_messages"]!!
        val adminWarn: YamlMap = warnMsg["admin"]!!

        // ban confirmation
        context.reply(String.format(adminWarn.getText("ban_confirmation"), subject.asMention, reason ?: adminErr.getText("ban_reason_null_or_too_large")))
            .setActionRow(Button.danger("ban-confirmation", "Ban!"))
            .queue()
    }

    /**
     * Checks if it's possible to ban the mentioned users.
     * @param context The interaction to retrieve info, check and reply.
     * @author Slz
     * @see execute
     */
    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val bot = context.guild!!.selfMember
        val subject = context.getOption("user")?.asMember
        val senderTopRole = sender.roles.sortedDescending()[0].position
        val botTopRole = bot.roles.sortedDescending()[0].position

        val errMsg: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = errMsg["generic"]!!
        val adminErr: YamlMap = errMsg["admin"]!!

        if (subject == null) {
            context.michiReply(String.format(genericErr.getText("user_not_found"), Emoji.michiShrug))
            return false
        }

        // checks if the agent is devil and is trying to ban michi >:(
        if (subject.id == config["BOT_ID"]) {
            context.michiReply(String.format(adminErr.getText("trying_to_ban_michi"), Emoji.michiUnimpressed))
            return false
        }

        // checks if the agent is trying to ban himself
        if (subject.user == sender.user) {
            context.michiReply(String.format(adminErr.getText("trying_selfban"), Emoji.michiHuh))
            return false
        }

        if (!sender.permissions.any(userPermissions::contains)) {
            context.michiReply(String.format(genericErr.getText("user_missing_perms"), Emoji.michiBlep))
            return false
        }

        if (subject.roles.any { role -> role.position >= senderTopRole || role.position >= botTopRole }) {
            context.michiReply(String.format(adminErr.getText("hierarchy_err"), subject.asMention))
            return false
        }

        return true
    }

}
