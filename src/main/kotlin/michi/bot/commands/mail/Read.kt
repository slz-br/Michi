package michi.bot.commands.mail

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button

@Suppress("Unused")
object Read: MichiCommand("read", "Reads a mail at a specific position of your inbox.", CommandScope.GLOBAL_SCOPE) {
    override val usage: String
        get() = "/read <position(optional)>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("position", "The position of the mail in your inbox.", OptionType.INTEGER, false)
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user

        if (!canHandle(context)) return

        val mailPosition = context.getOption("position")?.asInt?.minus(1) ?: 0
        val inbox = inboxMap[sender]!!

        if (!inbox[mailPosition].isSafe && !inbox[mailPosition].unknowLanguage) {

            val readAnywayButton = Button.danger("read-anyway", "Read anyway")
            val cancelReading = Button.secondary("cancel-reading", "Cancel")

            val embed = EmbedBuilder().apply {
                setTitle("This mails is unsafe ${Emoji.michiLook}")
                setDescription("`a unsafe mail means that the mail may contain toxicity, identity attack or be sexually explicit`")
                addField("Do you really want to read it? ${Emoji.michiThink}", "mail position: #${mailPosition + 1}", false)
                setFooter("You can either ignore it and remove the mail from your inbox or read it. If you read and it is indeed unsafe, feel free to report with /report-mail")
            }

            context.replyEmbeds(embed.build()).apply {
                setActionRow(
                    readAnywayButton,
                    cancelReading
                )
                setEphemeral(true)
            }.queue()
            return
        }

        context.reply("${inbox[mailPosition]}")
            .setEphemeral(true)
            .queue()

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val mailPostion = context.getOption("position")?.asInt?.minus(1) ?: 0
        val guild = context.guild
        val inbox = inboxMap[sender] ?: inboxMap.computeIfAbsent(sender) {
            val userInbox = mutableListOf<MailMessage>()
            userInbox
        }

        if (inbox.isEmpty()) {
            context.reply("Your inbox is empty ${Emoji.michiSad}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (mailPostion > inbox.size - 1 || mailPostion < 0) {
            context.reply("Invalid position ${Emoji.smolMichiAngry}")
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