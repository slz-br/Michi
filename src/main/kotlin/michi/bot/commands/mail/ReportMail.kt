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
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Color

@Suppress("Unused")
object ReportMail: MichiCommand("report-mail", GLOBAL_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Reports a mail from your inbox",
            DiscordLocale.ENGLISH_UK to "Reports a mail from your inbox",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Reporta uma carta do seu inbox"
        )


    override val arguments = listOf(
        MichiArgument(
            name = "position",
            descriptionLocalization = mapOf(
                DiscordLocale.ENGLISH_US to "The position in your inbox of the mail to report",
                DiscordLocale.ENGLISH_UK to "The position in your inbox of the mail to report",
                DiscordLocale.PORTUGUESE_BRAZILIAN to "A posição na sua inbox da carta para reportar"
            ),
            type = OptionType.INTEGER
        )
    )

    override val usage: String
        get() = "/$name <position>"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return

        val sender = context.user
        val position = context.getOption("position")!!.asInt - 1
        val inbox = inboxMap[sender]!!

        val reportButton = Button.danger("report-confirmation", "Report")
        val cancelButton = Button.secondary("cancel-report", "Cancel")

        val err: YamlMap = getYML(sender).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val warnMsg: YamlMap = getYML(sender).yamlMap["warn_messages"]!!
        val mailWarn: YamlMap = warnMsg["mail"]!!
        val genericWarn: YamlMap = warnMsg["generic"]!!
        val reportConfirmation = mailWarn.getText("mail_report_confirmation").split("\n")

        val embed = EmbedBuilder().apply {
            setColor(Color.RED)
            setTitle(reportConfirmation[0])
            setDescription(reportConfirmation[1])
            addField(reportConfirmation[2], String.format(reportConfirmation[3], position + 1, inbox[position].title), false)
            setFooter(reportConfirmation[4])
        }

        sender.openPrivateChannel().flatMap { it.sendMessageEmbeds(embed.build()).setActionRow(reportButton, cancelButton) }.queue(null) {
            context.michiReply(genericErr.getText("michi_is_dm_blocked"))
            return@queue
        }
        context.michiReply(genericWarn.getText("check_your_dm"))
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val position = context.getOption("position")?.asInt?.minus(1) ?: return false
        val guild = context.guild
        val inbox = inboxMap.computeIfAbsent(sender) {
            val userInbox = arrayListOf<MailMessage>()
            userInbox
        }

        val err: YamlMap = getYML(sender).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val mailErr: YamlMap = err["mail"]!!

        if (inbox.isEmpty()) {
            context.michiReply(String.format(mailErr.getText("custom_empty_inbox"), Emoji.michiHuh))
            return false
        }

        if (position > inbox.size - 1 || position < 0) {
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