package michi.bot.commands.mail

import kotlinx.coroutines.*
import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.config
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

val inboxMap = HashMap<User, MutableList<MailMessage>>()

@Suppress("Unused")
object Mail: MichiCommand("mail", "Sends an anonymous message to someone.", CommandScope.GLOBAL_SCOPE) {
    private val inMailCooldown = mutableSetOf<User>()

    override val usage: String
        get() = "/mail <title> <message> <receiver(the person to receive the mail)>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("title", "What is the mail about?", OptionType.STRING, isRequired = true, hasAutoCompletion = false),
            MichiArgument("message", "The message you want to send.", OptionType.STRING, isRequired = true, hasAutoCompletion = false),
            MichiArgument("receiver", "Who is this mail for?", OptionType.USER, isRequired = true, hasAutoCompletion = false)
        )

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val jda = context.jda
        val reportChannel = context.jda.getGuildById(config["BOT_SERVER_ID"])?.getTextChannelById("1085275324859818025") ?: return

        if (!canHandle(context)) return

        val receiver = context.getOption("receiver")!!.asUser
        val title = context.getOption("title")!!.asString
        val message = context.getOption("message")!!.asString

        var mailSent = false
        jda.retrieveUserById(receiver.id).queue(
            {
                // creating an inbox for the receiver if the receiver doesn't have one already
                val receiverInbox = inboxMap.computeIfAbsent(it) {
                    mutableListOf()
                }

                val mail = MailMessage(title, message, sender)

                // adding the mail to the receiver inbox
                receiverInbox += mail

                // reporting the mail to the report channel if it has an unknown language and is unsafe.
                if(!mail.isSafe && !mail.unknowLanguage) reportChannel.sendMessage("$mail\nsender: ${sender.asMention}\nreceiver: ${receiver.asMention}").queue()

                mailSent = true
                GlobalScope.launch { deleteMail(receiverInbox, mail) }

                context.reply("Mail sent ${Emoji.michiThumbsUp}").setEphemeral(true).queue()
            },
            {
                context.reply("Couldn't find the user ${Emoji.michiOpsie}").setEphemeral(true).queue()
            }
        )

        if (!mailSent) return

        cooldownForSendingMailManager(sender)
        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val receiver = context.getOption("receiver")?.asUser ?: return false
        val title = context.getOption("title")?.asString ?: return false
        val message = context.getOption("message")?.asString ?: return false

        if (sender == receiver) {
            context.reply("Are you trying to mail yourself? ${Emoji.michiHuh}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (receiver.id == config["BOT_ID"]) {
            context.reply("Sorry, I ain't open for mails ${Emoji.michiShrug}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (receiver.isBot) {
            context.reply("You can't mail bots, don't you have anybody else to mail?")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (message.isBlank() || title.isBlank()) {
            context.reply("Neither the title nor the message can be formed only by whitespaces.")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (message.length < 10) {
            context.reply("The message must have at least 10 characters.")
                .setEphemeral(true)
                .queue()
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