package michi.bot.commands.admin

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.CommandAutoCompletionListener
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.time.Duration

/**
 * Object for the mute command, a command that timeouts a user
 * if possible(case the sender of the command has permission to mute the user).
 * @author Slz
 */
@Suppress("Unused")
object Mute: MichiCommand("mute", GUILD_SCOPE) {
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

    override val usage: String
        get() = "/$name <user> <how much time>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("user", OptionType.USER),
            MichiArgument("time", OptionType.STRING, hasAutoCompletion = true)
        )

    /**
     * Mute the mentioned user(s) if possible.
     * @param context The Interaction to michiReply to.
     * @author Slz
     * @see canHandle
     */
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user

        if (!canHandle(context)) return

        val time = context.options[1].asString
        val memberToMute = context.options[0].asMember!!

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val adminSuccess: YamlMap = success["admin"]!!
        val other: YamlMap = getYML(context).yamlMap["other"]!!
        val timeUnits: YamlMap = other["time_units"]!!

        when (time) {
            "1 min" ->  {
                memberToMute.timeoutFor(Duration.ofMinutes(1)).queue()
                context.michiReply(String.format(adminSuccess.getText("mute_applied"), sender.asMention, memberToMute.asMention, "1 ${timeUnits.getText("minute")}"), false)
            }
            "5 min" ->  {
                memberToMute.timeoutFor(Duration.ofMinutes(5)).queue()
                context.michiReply(String.format(adminSuccess.getText("mute_applied"), sender.asMention, memberToMute.asMention, "5 ${timeUnits.getText("minute")}s"), false)
            }
            "10 min" -> {
                memberToMute.timeoutFor(Duration.ofMinutes(10)).queue()
                context.michiReply(String.format(adminSuccess.getText("mute_applied"), sender.asMention, memberToMute.asMention, "10 ${timeUnits.getText("minute")}s"), false)
            }
            "1 hour" -> {
                memberToMute.timeoutFor(Duration.ofHours(1)).queue()
                context.michiReply(String.format(adminSuccess.getText("mute_applied"), sender.asMention, memberToMute.asMention, "1 ${timeUnits.getText("hour")}"), false)
            }
            "1 day"  -> {
                memberToMute.timeoutFor(Duration.ofDays(1)).queue()
                context.michiReply(String.format(adminSuccess.getText("mute_applied"), sender.asMention, memberToMute.asMention, "1 ${timeUnits.getText("day")}"), false)
            }
            "1 week" -> {
                memberToMute.timeoutFor(Duration.ofDays(7)).queue()
                context.michiReply(String.format(adminSuccess.getText("mute_applied"), sender.asMention, memberToMute.asMention, "1 ${timeUnits.getText("week")}"), false)
            }
        }
    }

    /**
     * Checks if it is possible to ban the subjects.
     * @param context The SlashCommandInteraction that called the ban function.
     * @return True if all members can be banned, false if not.
     * @author Slz
     * @see execute
     */
    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild!!
        val sender = context.member!!
        val time = context.getOption("time")!!.asString
        val bot = guild.selfMember
        val memberToMute = context.options[0].asMember
        val senderTopRole = sender.roles.sortedDescending()[0].position
        val botTopRole = bot.roles.sortedDescending()[0].position

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val adminErr: YamlMap = err["admin"]!!

        if (memberToMute == null) {
            context.michiReply(String.format(genericErr.getText("user_not_found"), Emoji.michiShrug))
            return false
        }

        if (memberToMute.user == sender) {
            context.michiReply(String.format(adminErr.getText("trying_selfmute"), Emoji.michiHuh))
            return false
        }

        if (memberToMute == bot) {
            context.michiReply(String.format(adminErr.getText("trying_to_mute_michi"), Emoji.michiHMPH))
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        if (!sender.permissions.any(userPermissions::contains)) {
            context.michiReply(String.format(genericErr.getText("user_missing_perms"), Emoji.michiBlep))
            return false
        }

        if (memberToMute.roles.any { role -> role.position >= senderTopRole || role.position >= botTopRole }) {
            context.michiReply(String.format(adminErr.getText("hierarchy_err"), memberToMute.asMention))
            return false
        }

        if (time !in CommandAutoCompletionListener.timeAutoCompletion) {
            context.michiReply(String.format(genericErr.getText("option_err"), Emoji.smolMichiAngry))
            return false
        }

        return true
    }

}