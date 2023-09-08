package michi.bot.commands.mail

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GLOBAL_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.components.buttons.Button

/**
 * Object for the clear-inbox command, a command to clear the mail inbox of the user.
 * @author Slz
 */
@Suppress("Unused")
object ClearInbox: MichiCommand("clear-inbox", GLOBAL_SCOPE) {
    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Clears all mails from your inbox",
            DiscordLocale.ENGLISH_UK to "Clears all mails from your inbox",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Limpa todas as cartas da sua inbox"
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        val sender = context.user

        val deleteButton = Button.danger("clear-mail-inbox-confirmation", "Delete")
        val cancelDeletion = Button.secondary("cancel-mail-inbox-clearing", "Cancel")

        val warnMsg: YamlMap = getYML(sender).yamlMap["warn_messages"]!!
        val mailWarn: YamlMap = warnMsg["mail"]!!
        val genericWarn: YamlMap = warnMsg["generic"]!!
        val err: YamlMap = getYML(sender).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!

        sender.openPrivateChannel().flatMap { privtaChannel ->
            privtaChannel.sendMessage(mailWarn.getText("clear_inbox_confirmation"))
                .setActionRow(deleteButton, cancelDeletion)
        }.queue(null) {
            context.michiReply(genericErr.getText("michi_is_dm_blocked"))
            return@queue
        }

        context.michiReply(genericWarn.getText("check_your_dm"))
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val guild = context.guild

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val mailErr: YamlMap = err["mail"]!!

        val inbox = inboxMap.computeIfAbsent(sender) {
            mutableListOf()
        }

        if (inbox.isEmpty()) {
            context.michiReply(String.format(mailErr.getText("empty_inbox"), Emoji.michiSad))
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