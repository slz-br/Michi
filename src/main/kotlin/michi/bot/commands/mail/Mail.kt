package michi.bot.commands.mail

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import michi.bot.config
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import java.awt.Color


object Mail {
    val inboxMap = HashMap<User, MutableList<MailMessage>>()
    private val inMailCooldown = mutableSetOf<User>()

    private suspend fun deleteMail(inbox: MutableList<MailMessage>, mailToRemove: MailMessage) {
        delay((60000 /*1 minute*/ * 60 /*1 hour*/ * 24 /*1 day*/ * 4 /* amount of days*/).toLong())
        if (inbox.any { mail -> mail == mailToRemove }) inbox.remove(mailToRemove)
    }

    private suspend fun cooldownForSendingMailManager(user: User) {
        inMailCooldown.add(user)
        delay((60000 /*1 minute*/ * 60 /*1 hour*/ * 4 /*amount of hours*/).toLong())
        inMailCooldown.remove(user)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun writeMail(context: SlashCommandInteractionEvent) {
        val sender = context.user

        if (inMailCooldown.contains(sender)) {
            context.reply("You are in cooldown for sending mails(you can send a mail every 4 and a half hours)")
                .setEphemeral(true)
                .queue()
            return
        }

        val title = TextInput.create("title", "Title", TextInputStyle.SHORT)
            .setMinLength(1)
            .setMaxLength(127)
            .setPlaceholder("What's the mail about?")
            .setRequired(true)
            .build()

        val message = TextInput.create("message", "Message", TextInputStyle.PARAGRAPH)
            .setMinLength(10)
            .setMaxLength(1250)
            .setPlaceholder("The message you want to send.")
            .setRequired(true)
            .build()

        val userID = TextInput.create("user", "UserID", TextInputStyle.SHORT)
            .setMinLength(18)
            .setMaxLength(18)
            .setPlaceholder("example: 858866079756713995")
            .setRequired(true)
            .build()

        val modal = Modal.create("mail", "Anonymous Mail Service")
            .addActionRows(
                ActionRow.of(title),
                ActionRow.of(message),
                ActionRow.of(userID)
            )
            .build()

        context.replyModal(modal).queue()

        // puts the user that sent the command in cooldown
        GlobalScope.launch { SlashCommandListener.cooldownManager(sender) }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendMail(context: ModalInteractionEvent) {
        val sender = context.user
        val title = context.values[0].asString
        val message = context.values[1].asString
        val receiverID = context.values[2].asString
        val reportChannel = context.jda.getGuildById(config["BOT_SERVER_ID"])!!.getTextChannelById("1085275324859818025")!!

        if (message.isBlank() || title.isBlank()) {
            context.reply("Neither the title nor the message can be formed only by whitespaces.").setEphemeral(true).queue()
            return
        }

        if (sender.id == receiverID) {
            context.reply("Are you trying to mail yourself? ${Emoji.michiHuh}")
                .setEphemeral(true)
                .queue()
            return
        }

        context.jda.retrieveUserById(receiverID).queue(
            {
                val receiverInbox = inboxMap.computeIfAbsent(it) {
                    val userInbox: MutableList<MailMessage> = ArrayList()
                    userInbox
                }
                val mailMessage = MailMessage(title, message, sender)
                mailMessage.message = "$title, $message"
                if (!mailMessage.isSafe && !mailMessage.unknowLanguage) reportChannel.sendMessage("$mailMessage\nsender: ${mailMessage.sender.asMention}\nreceiver: $receiverID").queue()

                receiverInbox.add(mailMessage)
                context.reply("Mail sent!").setEphemeral(true).queue()

                // puts the user that sent the command in cooldown
                GlobalScope.launch {
                    cooldownForSendingMailManager(sender)
                    deleteMail(receiverInbox, mailMessage)
                    SlashCommandListener.cooldownManager(sender)
                }

            },
            {
                context.reply("Couldn't find the user ${Emoji.michiOpsie}")
                    .setEphemeral(true)
                    .queue()
            }

        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendMail(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val title = context.options[0].asString
        val message = context.options[1].asString
        val receiver = context.options[2].asUser
        val reportChannel = context.jda.getGuildById(config["BOT_SERVER_ID"])!!.getTextChannelById("1085275324859818025")!!

        if (title.isBlank() || message.isBlank()) {
            context.reply("Neither the message nor the title can be formed only by whitespaces").setEphemeral(true).queue()
            return
        }

        if (message.length < 10) {
            context.reply("Message must be at least 10 characters long.").setEphemeral(true).queue()
            return
        }

        if (sender == receiver) {
            context.reply("Are you trying to mail yourself? ${Emoji.michiHuh}").setEphemeral(true).queue()
            return
        }

        val receiverInbox = inboxMap.computeIfAbsent(receiver) {
            val userInbox: MutableList<MailMessage> = ArrayList()
            userInbox
        }

        val mailMessage = MailMessage(title, message, sender)
        if (!mailMessage.isSafe && !mailMessage.unknowLanguage) reportChannel.sendMessage("$mailMessage\nsender: ${mailMessage.sender.asMention}\nreceiver: $receiver\n``this is an automatic report.``").queue()

        receiverInbox.add(mailMessage)
        context.reply("Mail sent!").setEphemeral(true).queue()

        // puts the user that sent the command in cooldown
        GlobalScope.launch {
            cooldownForSendingMailManager(sender)
            deleteMail(receiverInbox, mailMessage)
            SlashCommandListener.cooldownManager(sender)
        }

    }

    /**
     * Checks a page of the user mail inbox.
     * @param context The interaction to retrieve info and reply to.
     * @author Slz
     */

    @OptIn(DelicateCoroutinesApi::class)
    fun inbox(context: SlashCommandInteractionEvent) {
        val user = context.user
        val mailsPerPage = 5
        val page = context.options[0].asInt - 1
        val hook = context.interaction.hook
        val mails: MutableList<MailMessage> = inboxMap.computeIfAbsent(user) {
            val userMails: MutableList<MailMessage> = ArrayList()
            userMails
        }
        context.deferReply(true).queue()

        val pagesCount = if (mails.size != 0 && mails.size / mailsPerPage != 0) mails.size / mailsPerPage else 1
        if (page > pagesCount || page < 0) {
            context.reply("Invalid page.").setEphemeral(true).queue()
            return
        }

        val embed = EmbedBuilder()
        embed.setColor(Color.BLACK)
            .setTitle("Your inbox")
            .setFooter("page ${page + 1} of $pagesCount")
        if (mails.isEmpty()) embed.addField("You don't have any mail ${Emoji.michiSaddened}", "come back later", false)
        else {
            var warning: String? = null
            var currentMail: MailMessage
            for (i in 0..mailsPerPage) {
                if (i <= mails.size - 1) {
                    currentMail = mails[page * mailsPerPage + i]

                    if (currentMail.containsLink) warning = "> ⚠️ contains links, be cautious, links can't be auto filtered. If the link breaks Michi's TOS, feel free to ``/report`` it."
                    if (!currentMail.isSafe) warning = "> ❗ May be inappropiate - It's suggested to wait for feedback or remove it from your inbox with ``/remove``. Read it at your own risk."
                    if (currentMail.unknowLanguage) warning = "> ❓ Unknow language. Unable to verify the mail(since it isn't in English). If you find something inappropriate, feel free to ``/report`` it."
                    embed.addField("#${(page * mailsPerPage + i) + 1} ${currentMail.title}", warning?: "✅ Mail is safe", false)
                }
            }
        }

        hook.sendMessageEmbeds(embed.build()).setEphemeral(true).queue()

        // puts the user that sent the command in cooldown
        GlobalScope.launch { SlashCommandListener.cooldownManager(user) }
    }

    /**
     * Read a mail from the user's inbox at a specific index.
     * @param context The interaction to retrieve info and reply to.
     * @author Slz
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun read(context: SlashCommandInteractionEvent) {
        val user = context.user
        val mailIndex = context.options[0].asInt - 1
        val mails: MutableList<MailMessage> = inboxMap.computeIfAbsent(user) {
            val userMails: MutableList<MailMessage> = ArrayList()
            userMails
        }

        if (mails.isEmpty()) {
            context.reply("Your inbox is empty ${Emoji.michiSad}").setEphemeral(true).queue()
            return
        }

        if (mailIndex > mails.size || mailIndex < 0) {
            context.reply("Invalid position").setEphemeral(true).queue()
            return
        }
        context.reply(mails[mailIndex].toString()).setEphemeral(true).queue()

        // puts the user that sent the command in cooldown
        GlobalScope.launch { SlashCommandListener.cooldownManager(user) }
    }

    /**
     * Removes a mail at a specific index from the user's inbox.
     * @param context The interaction to retrieve info and reply to.
     * @author Slz
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun remove(context: SlashCommandInteractionEvent) {
        val user = context.user
        val mailIndex = context.options[0].asInt - 1
        val mails: MutableList<MailMessage> = inboxMap.computeIfAbsent(user) {
            val userMails: MutableList<MailMessage> = ArrayList()
            userMails
        }

        if (mails.isEmpty()) {
            context.reply("Your inbox is empty ${Emoji.michiSad}").setEphemeral(true).queue()
            return
        }

        if (mailIndex >= mails.size || mailIndex < 0) {
            context.reply("Invalid position").setEphemeral(true).queue()
            return
        }

        // removing the mail
        mails.removeAt(mailIndex)

        // answering the event
        context.reply("Removed the mail at position ${mailIndex + 1} from your inbox").setEphemeral(true).queue()

        // puts the user that sent the command in cooldown
        GlobalScope.launch { SlashCommandListener.cooldownManager(user) }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun clearInbox(context: SlashCommandInteractionEvent) {
        val user = context.user
        val mails: MutableList<MailMessage> = inboxMap.computeIfAbsent(user) {
            val userMails: MutableList<MailMessage> = ArrayList()
            userMails
        }

        if (mails.isEmpty()) {
            context.reply("Your inbox is empty ${Emoji.michiSad}").setEphemeral(true).queue()
            return
        }

        mails.clear()

        val deleteButton = Button.danger("clear-mail-inbox-confirmation", "Delete")
        val cancelDeletion = Button.secondary("cancel-mail-inbox-clearing", "Cancel")

        context.reply("Are you sure you want to clear your inbox?")
            .setActionRow(deleteButton, cancelDeletion)
            .setEphemeral(true)
            .queue()

        GlobalScope.launch { SlashCommandListener.cooldownManager(user)}

    }

    @OptIn(DelicateCoroutinesApi::class)
    fun report(context: SlashCommandInteractionEvent) {
        val user = context.user
        val mailToReportIndex = context.options[0].asInt - 1

        val mails: MutableList<MailMessage> = inboxMap.computeIfAbsent(user) {
            val userMails: MutableList<MailMessage> = ArrayList()
            userMails
        }

        if (mails.isEmpty()) {
            context.reply("Your inbox is empty ${Emoji.michiSad}").setEphemeral(true).queue()
            return
        }

        if (mailToReportIndex > mails.size || mailToReportIndex < 0) {
            context.reply("Invalid position.").setEphemeral(true).queue()
            return
        }

        val reportButton = Button.danger("report-confirmation", "Report")
        val cancelButton = Button.secondary("cancel-report", "Cancel")

        val embed = EmbedBuilder()
        embed.setColor(Color.RED)
            .setTitle("Report")
            .setDescription("Do you really want to report this mail? If so, are you sure that this is the right mail?")
            .addField("Mail to report: ", "#${mailToReportIndex + 1} - ${mails[mailToReportIndex].title}", false)
            .setFooter("After the report, the mail will be checked and we'll send you a feedback message if possible.\nPlease note that: intentional fake reports may result in punishments for you.")

        context.replyEmbeds(embed.build())
            .setActionRow(reportButton, cancelButton)
            .setEphemeral(true)
            .queue()

        GlobalScope.launch { SlashCommandListener.cooldownManager(user) }

    }

}