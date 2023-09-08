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

/**
 * Object for the read command, a command that reads a mail from a specific position of a user's inbox
 * @author Slz
 */
@Suppress("Unused")
object Read: MichiCommand("read", GLOBAL_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Reads a mail from inbox",
            DiscordLocale.ENGLISH_UK to "Reads a mail from inbox",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Lê uma carta do seu inbox"
        )

    override val usage: String
        get() = "/$name <position(optional)>"

    override val arguments = listOf(
        MichiArgument(
            name = "position",
            descriptionLocalization = mapOf(
                DiscordLocale.ENGLISH_US to "The position of the mail to read in your inbox",
                DiscordLocale.ENGLISH_UK to "The postion of the mail to read in your inbox",
                DiscordLocale.PORTUGUESE_BRAZILIAN to "A posição da carta para ler na seu inbox"
            ),
            type = OptionType.INTEGER,
            isRequired = false
        )
    )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return

        val sender = context.user
        val mailPosition = context.getOption("position")?.asInt?.minus(1) ?: 0
        val inbox = inboxMap[sender]!!

        if (!inbox[mailPosition].isSafe && !inbox[mailPosition].unknownLanguage) {

            val readAnywayButton = Button.danger("read-anyway", "Read anyway")
            val cancelReading = Button.secondary("cancel-reading", "Cancel")

            val warnMsg: YamlMap = getYML(sender).yamlMap["warn_messages"]!!
            val mailWarn: YamlMap = warnMsg["mail"]!!
            val genericWarn: YamlMap = warnMsg["generic"]!!
            val readUnsafeConfirmation = mailWarn.getText("read_unsafe_mail_confirmation").split('\n')

            val embed = EmbedBuilder().apply {
                setTitle(String.format(readUnsafeConfirmation[0], Emoji.michiLook))
                setDescription(readUnsafeConfirmation[1])
                addField(String.format(readUnsafeConfirmation[2], Emoji.michiThink), String.format(readUnsafeConfirmation[3], mailPosition + 1), false)
                setFooter(readUnsafeConfirmation[4])
            }

            context.michiReply(genericWarn.getText("check_your_dm"))

            sender.openPrivateChannel().flatMap {
                it.sendMessageEmbeds(embed.build())
                    .setActionRow(readAnywayButton, cancelReading)
            }.queue()
            return
        }

        context.michiReply("${inbox[mailPosition]}")
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val mailPostion = context.getOption("position")?.asInt?.minus(1) ?: 0
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

        if (mailPostion > inbox.size - 1 || mailPostion < 0) {
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