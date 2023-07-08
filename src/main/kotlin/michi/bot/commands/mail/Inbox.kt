package michi.bot.commands.mail

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.awt.Color

@Suppress("Unused")
object Inbox: MichiCommand("inbox", "Checks for emails that you've received", CommandScope.GLOBAL_SCOPE) {
    private const val MAILS_PER_PAGE = 5

    override val usage: String
        get() = "/inbox <page(optional)>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("page", "The page of your inbox to see.", OptionType.INTEGER, isRequired = false, hasAutoCompletion = false)
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user

        if (!canHandle(context)) return

        val page = context.getOption("page")?.asInt?.minus(1) ?: 0
        val inbox = inboxMap[sender] ?: inboxMap.computeIfAbsent(sender) {
            val userInbox = arrayListOf<MailMessage>()
            userInbox
        }
        val pagesCount = if (inbox.size != 0 && inbox.size / MAILS_PER_PAGE != 0) inbox.size / MAILS_PER_PAGE else 1

        val embed = EmbedBuilder().apply {
            setColor(Color.BLACK)
            setTitle("Your inbox")
            setFooter("page ${page + 1} of $pagesCount")
        }

        if (inbox.isEmpty()) embed.addField("You don't have any mail ${Emoji.michiSaddened}", "come back later", false)
        else {
            var warning: String? = null
            var currentMail: MailMessage
            for (i in 0..MAILS_PER_PAGE) {
                if (i <= inbox.size - 1) {
                    currentMail = inbox[page * MAILS_PER_PAGE + i]

                    if (currentMail.containsLink) warning = "> ⚠️ contains links, be cautious, links can't be auto filtered. If the link breaks Michi's TOS, feel free to ``/report`` it."
                    if (!currentMail.isSafe && !currentMail.unknowLanguage) warning = "> ❗ May be inappropiate - It's suggested to wait for feedback or remove it from your inbox with ``/remove``. Read it at your own risk."
                    if (currentMail.unknowLanguage) warning = "> ❓ Unknow language. Unable to verify the mail(since it isn't in English). If you find something inappropriate, feel free to ``/report`` it."
                    embed.addField("# ${(page * MAILS_PER_PAGE + i) + 1} - ${currentMail.title}", warning?: "✅ Mail is safe", false)
                }
            }
        }

        context.replyEmbeds(embed.build()).setEphemeral(true).queue()
        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val page = context.getOption("page")?.asInt?.minus(1) ?: 0
        val guild = context.guild
        val inbox = inboxMap[sender] ?: inboxMap.computeIfAbsent(sender) {
            val userInbox = arrayListOf<MailMessage>()
            userInbox
        }
        val pagesCount = if (inbox.size != 0 && inbox.size / MAILS_PER_PAGE != 0) inbox.size / MAILS_PER_PAGE else 1

        if (page > pagesCount || page < 0) {
            context.reply("Invalid position")
                .setEphemeral(true)
                .queue()
            return false
        }

        guild?.run {
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