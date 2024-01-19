package michi.bot.commands.mail

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import michi.bot.commands.CommandScope.GLOBAL_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.config
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * Object for the mail command, a command that sends a mail to an inbox(check [inboxMap]) of another
 * user.
 * @author Slz
 */
@Suppress("Unused")
object Mail: MichiCommand("mail", GLOBAL_SCOPE) {
    private val inMailCooldown = mutableSetOf<User>()
    val inboxMap = HashMap<User, MutableList<MailMessage>>()

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Sends an anonymous message to someone",
            DiscordLocale.ENGLISH_UK to "Sends an anonymous message to someone",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Envia uma carta anônima para alguém"
        )

    override val usage: String
        get() = "/$name <title> <message> <receiver(the person to receive the mail)>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument(
                name = "title",
                descriptionLocalization = mapOf(
                    DiscordLocale.ENGLISH_US to "What is the mail about?",
                    DiscordLocale.ENGLISH_UK to "What is the mail about?",
                    DiscordLocale.PORTUGUESE_BRAZILIAN to "Sobre o que é a carta?"
                ),
                type = OptionType.STRING
            ),
            MichiArgument(
                name = "message",
                descriptionLocalization = mapOf(
                    DiscordLocale.ENGLISH_US to "The body of the mail",
                    DiscordLocale.ENGLISH_UK to "The body of the mail",
                    DiscordLocale.PORTUGUESE_BRAZILIAN to "O corpo da carta"
                ),
                type = OptionType.STRING
            ),
            MichiArgument(
                name = "receiver",
                descriptionLocalization = mapOf(
                    DiscordLocale.ENGLISH_US to "Who is this mail for?",
                    DiscordLocale.ENGLISH_UK to "Who is this mail for?",
                    DiscordLocale.PORTUGUESE_BRAZILIAN to "Para quem é essa carta?"
                ),
                type = OptionType.USER
            )
        )

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val jda = context.jda
        val reportChannel = context.jda.getGuildById(config["BOT_SERVER_ID"])?.getTextChannelById(config["MAIL_REPORT_CHANNEL_ID"]) ?: return

        if (!canHandle(context)) return

        val err: YamlMap = getYML(sender).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val success: YamlMap = getYML(sender).yamlMap["success_messages"]!!
        val mailSuccess: YamlMap = success["mail"]!!

        val receiver = context.getOption("receiver")!!.asUser
        val title = context.getOption("title")!!.asString
        val message = context.getOption("message")!!.asString

        var mailSent = false
        jda.retrieveUserById(receiver.id).queue(
            { receiverUser ->
                // creating an inbox for the receiver if the receiver doesn't have one already
                val receiverInbox = inboxMap.computeIfAbsent(receiverUser) {
                    mutableListOf()
                }

                val mail = MailMessage(title, message, sender)

                // adding the mail to the receiver inbox
                receiverInbox += mail

                // reporting the mail to the report channel if it has an unknown language and is unsafe.
                if(!mail.isSafe && !mail.unknownLanguage) reportChannel.sendMessage("$mail\nsender: ${sender.asMention}\nreceiver: ${receiver.asMention}").queue()

                mailSent = true
                GlobalScope.launch { deleteMail(receiverInbox, mail) }

                context.michiReply(String.format(mailSuccess.getText("mail_sent"), Emoji.michiThumbsUp))
            },
            {
                context.michiReply(String.format(genericErr.getText("custom_user_not_found"), Emoji.michiOpsie))
            }
        )

        if (!mailSent) return

        Mutex().withLock {
            cooldownForSendingMailManager(sender)
        }
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val receiver = context.getOption("receiver")?.asUser!!
        val title = context.getOption("title")?.asString!!
        val message = context.getOption("message")?.asString!!

        val err: YamlMap = getYML(sender).yamlMap["error_messages"]!!
        val mailErr: YamlMap = err["mail"]!!

        if (sender == receiver) {
            context.michiReply(String.format(mailErr.getText("trying_selfmail"), Emoji.michiHuh))
            return false
        }

        if (receiver.id == config["BOT_ID"]) {
            context.michiReply(String.format(mailErr.getText("trying_to_mail_michi"), Emoji.michiShrug))
            return false
        }

        if (receiver.isBot) {
            context.michiReply(mailErr.getText("trying_to_mail_bot"))
            return false
        }

        if (message.isBlank() || title.isBlank()) {
            context.michiReply(mailErr.getText("blank_mail"))
            return false
        }

        if (message.length < 10) {
            context.michiReply(mailErr.getText("invalid_message_length"))
            return false
        }

        if (sender in inMailCooldown) {
            context.michiReply(mailErr.getText("in_mail_cooldown"))
            return false
        }

        return true
    }

    private suspend fun cooldownForSendingMailManager(user: User) {
        inMailCooldown += user
        delay((60000 /*1 minute*/ * 60 /*1 hour*/ * 4 /* amount of hours */).toLong())
        inMailCooldown -= user
    }

    private suspend fun deleteMail(inbox: MutableList<MailMessage>, mailToRemove: MailMessage) {
        delay((60000 /*1 minute*/ * 60 /*1 hour*/ * 24 /*1 day*/ * 4 /* amount of days*/).toLong())
        inbox -= mailToRemove
    }

}