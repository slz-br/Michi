package michi.bot.commands.admin

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandDeactivated
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.CommandAutoCompletionListener
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

// todo: add a button that ends the poll immediately.

@CommandDeactivated
object Poll: MichiCommand("poll", GUILD_SCOPE) {

    override val userPermissions =
        listOf(
            Permission.MANAGE_SERVER,
            Permission.ADMINISTRATOR,
            Permission.MANAGE_EVENTS
        )

    override val arguments: List<MichiArgument> =
        listOf(
            MichiArgument("option-1", OptionType.STRING),
            MichiArgument("option-2", OptionType.STRING),
            MichiArgument("time", OptionType.STRING, hasAutoCompletion = true),
        )

    override val usage = "/$name <option-1> <option-2> <time>"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return

        TODO("Not yet implemented")
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val bot = context.guild!!.selfMember
        val option1 = context.getOption("option 1")!!.asString
        val option2 = context.getOption("option 2")!!.asString
        val time = context.getOption("time")!!.asString

        val err: YamlMap = ReplyUtils.getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val adminErr: YamlMap = err["admin"]!!

        if ((option1.length + option2.length) > 5500) {
            context.michiReply(String.format(adminErr.getText("poll_char_limit_exceeded"), 5500, Emoji.michiThink))
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

        if (time !in CommandAutoCompletionListener.pollAutoCompletion) {
            context.michiReply(String.format(genericErr.getText("option_err"), Emoji.smolMichiAngry))
            return false
        }

        return true
    }
}