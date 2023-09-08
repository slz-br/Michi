package michi.bot.commands.mail

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GLOBAL_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType

@Suppress("Unused")
object RemoveMail: MichiCommand("remove-mail", GLOBAL_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Removes a mail from your inbox",
            DiscordLocale.ENGLISH_UK to "Removes a mail from your inbox",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Remove uma carta do seu inbox"
        )

    override val usage: String
        get() = "/$name <position(the position of the mail in your inbox)>"

    override val arguments = listOf(
        MichiArgument(
            name = "position",
            descriptionLocalization = mapOf(
                DiscordLocale.ENGLISH_US to "The position in your inbox of the mail to remove",
                DiscordLocale.ENGLISH_UK to "The position in your inbox of the mail to remove",
                DiscordLocale.PORTUGUESE_BRAZILIAN to "A posição no seu inbox da carta para remover"
            ),
            type = OptionType.INTEGER
        )
    )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return

        val sender = context.user
        val mailIndex = context.getOption("position")!!.asInt - 1

        inboxMap.computeIfAbsent(sender) {
            val userInbox = arrayListOf<MailMessage>()
            userInbox
        }.removeAt(mailIndex)

        val success: YamlMap = getYML(sender).yamlMap["success_messages"]!!
        val mailSuccess: YamlMap = success["mail"]!!

        context.michiReply(String.format(mailSuccess.getText("mail_removed"), mailIndex + 1))
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val mailIndex = context.getOption("position")!!.asInt.minus(1)
        val guild = context.guild
        val inbox = inboxMap.computeIfAbsent(sender) {
            mutableListOf()
        }

        val err: YamlMap = getYML(sender).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val mailErr: YamlMap = err["mail"]!!

        if (inbox.isEmpty()) {
            context.michiReply(String.format(mailErr.getText("empty_inbox"), Emoji.michiSad))
            return false
        }

        if (mailIndex >= inbox.size - 1 || mailIndex < 0) {
            context.michiReply(genericErr.getText("invalid_position"))
            return false
        }

        guild?.let {
            val bot = guild.selfMember
            if (!bot.permissions.containsAll(botPermissions)) {
                context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
                return false
            }
        }

        return true
    }

}