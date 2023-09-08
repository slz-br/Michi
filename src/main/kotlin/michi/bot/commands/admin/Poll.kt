package michi.bot.commands.admin

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandDeactivated
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

// todo: add a button that ends the poll immediately.

@CommandDeactivated
object Poll: MichiCommand("poll", GUILD_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Creates a poll for users to vote",
            DiscordLocale.ENGLISH_UK to "Creates a poll for users to vote",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Cria uma enquete para usuários votarem"
        )

    override val userPermissions =
        listOf(
            Permission.MANAGE_SERVER,
            Permission.ADMINISTRATOR,
            Permission.MANAGE_EVENTS
        )

    override val arguments: List<MichiArgument> =
        listOf(
            MichiArgument(
                name = "option-1",
                descriptionLocalization = mapOf(
                    DiscordLocale.ENGLISH_US to "The 1st option for users to choose",
                    DiscordLocale.ENGLISH_UK to "The 1st option for users to choose",
                    DiscordLocale.PORTUGUESE_BRAZILIAN to "A 1ª opção para usuários escolherem"
                ),
                type = OptionType.STRING
            ),
            MichiArgument(
                name = "option-2",
                descriptionLocalization = mapOf(
                    DiscordLocale.ENGLISH_US to "The 2nd option for users to choose",
                    DiscordLocale.ENGLISH_UK to "The 2nd option for users to choose",
                    DiscordLocale.PORTUGUESE_BRAZILIAN to "A 2ª opção para usuários escolherem"
                ),
                type = OptionType.STRING
            ),
            MichiArgument(
                name = "time",
                descriptionLocalization = mapOf(
                    DiscordLocale.ENGLISH_US to "How long the poll will last",
                    DiscordLocale.ENGLISH_UK to "How long the poll will last",
                    DiscordLocale.PORTUGUESE_BRAZILIAN to "Por quanto tempo a enquete vai durar"
                ),
                type = OptionType.STRING,
                hasAutoCompletion = true
            ),
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

        val err: YamlMap = getYML(sender.user).yamlMap["error_messages"]!!
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