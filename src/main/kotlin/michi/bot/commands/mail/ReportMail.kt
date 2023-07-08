package michi.bot.commands.mail

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Color

@Suppress("Unused")
object ReportMail: MichiCommand("report-mail", "Reports a mail that was sent to you.", CommandScope.GLOBAL_SCOPE) {

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_SEND_IN_THREADS
        )
    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("position", "The position of the mail to report", OptionType.INTEGER, isRequired = true, hasAutoCompletion = false)
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user

        if (!canHandle(context)) return

        val position = context.getOption("position")!!.asInt - 1
        val inbox = inboxMap[sender]!!

        val reportButton = Button.danger("report-confirmation", "Report")
        val cancelButton = Button.secondary("cancel-report", "Cancel")

        val embed = EmbedBuilder().apply {
            setColor(Color.RED)
            setTitle("Report")
            setDescription("Do you really want to report this mail? If so, are you sure that this is the right mail?")
            addField("Mail to report: ", "#${position + 1} - ${inbox[position].title}", false)
            setFooter("After the report, the mail will be checked and we'll send you a feedback message if possible.\nPlease note that: intentional fake reports may result in punishments for you.")
        }

        context.replyEmbeds(embed.build()).setActionRow(reportButton, cancelButton)
            .setEphemeral(true)
            .queue()

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val position = context.getOption("position")?.asInt?.minus(1) ?: return false
        val guild = context.guild
        val inbox = inboxMap[sender] ?: inboxMap.computeIfAbsent(sender) {
            val userInbox = arrayListOf<MailMessage>()
            userInbox
        }

        if (inbox.isEmpty()) {
            context.reply("Your inbox is empty, what are you trying to report? ${Emoji.michiHuh}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (position > inbox.size - 1 || position < 0) {
            context.reply("Invalid position")
                .setEphemeral(true)
                .queue()
            return false
        }

        guild?.let {
            val bot = guild.selfMember
            if (!bot.permissions.containsAll(botPermissions)) {
                context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}")
                    .setEphemeral(true)
                    .queue()
                return false
            }
        }

        return true
    }
}