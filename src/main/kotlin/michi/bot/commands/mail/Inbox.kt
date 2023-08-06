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
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.awt.Color

/**
 * Object for the inbox command, a command that displays all the mails of the user.
 * @author Slz
 */
@Suppress("Unused")
object Inbox: MichiCommand("inbox", GLOBAL_SCOPE) {
    private const val MAILS_PER_PAGE = 5

    override val usage: String
        get() = "/$name <page(optional)>"

    override val arguments = listOf(MichiArgument("page", OptionType.INTEGER, false))

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user

        if (!canHandle(context)) return

        val warnMsg: YamlMap = getYML(context).yamlMap["warn_messages"]!!
        val mailWarn: YamlMap = warnMsg["mail"]!!

        val page = context.getOption("page")?.asInt?.minus(1) ?: 0
        val inbox = inboxMap.computeIfAbsent(sender) {
            val userInbox = arrayListOf<MailMessage>()
            userInbox
        }
        val pagesCount = if (inbox.size != 0 && inbox.size / MAILS_PER_PAGE != 0) inbox.size / MAILS_PER_PAGE else 1

        val embed = EmbedBuilder().apply {
            setColor(Color.BLACK)
            setTitle("Your inbox")
            setFooter("page ${page + 1} of $pagesCount")
        }

        val emptyInboxWarn = mailWarn.getText("empty_inbox").split("\n")

        if (inbox.isEmpty()) embed.addField(String.format(emptyInboxWarn[0], Emoji.michiSaddened), emptyInboxWarn[1], false)
        else {
            var warning: String? = null
            var currentMail: MailMessage
            for (i in 0..MAILS_PER_PAGE) {
                if (i <= inbox.size - 1) {
                    currentMail = inbox[page * MAILS_PER_PAGE + i]

                    if (currentMail.containsLink) warning = mailWarn.getText("contains_link")
                    if (currentMail.unknownLanguage) warning = mailWarn.getText("unknown_content")
                    if (!currentMail.isSafe && !currentMail.unknownLanguage) warning = mailWarn.getText("possibly_inappropriate")
                    embed.addField("# ${(page * MAILS_PER_PAGE + i) + 1} - ${currentMail.title}", warning?: mailWarn.getText("mail_is_safe"), false)
                }
            }
        }

        context.michiReply(embed.build())
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val page = context.getOption("page")?.asInt?.minus(1) ?: 0
        val guild = context.guild
        val inbox = inboxMap.computeIfAbsent(sender) {
            val userInbox = arrayListOf<MailMessage>()
            userInbox
        }
        val pagesCount = if (inbox.size != 0 && inbox.size / MAILS_PER_PAGE != 0) inbox.size / MAILS_PER_PAGE else 1

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!

        if (page > pagesCount || page < 0) {
            context.michiReply(genericErr.getText("invalid_page"))
            return false
        }

        guild?.run {
            val bot = guild.selfMember
            if (!bot.permissions.containsAll(botPermissions)) {
                context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
                return false
            }
        }

        return true
    }
}