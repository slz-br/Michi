package michi.bot.commands.admin

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * Object for the "clear" command, a command that tries to delete a specified amount of messages(between 1 and 100)
 * @author Slz
 */
@Suppress("Unused")
object Clear: MichiCommand("clear", GUILD_SCOPE) {
    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Deletes an amount of messages from this chat",
            DiscordLocale.ENGLISH_UK to "Deletes an amount of messages from this chat",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Deleta uma quantidade de mensagens desse canal"
        )

    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR,
            Permission.MESSAGE_MANAGE
        )

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_SEND_IN_THREADS,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_HISTORY
        )

    override val arguments = listOf(
        MichiArgument(
            name = "amount",
            descriptionLocalization = mapOf(
                DiscordLocale.ENGLISH_US to "Amount of messages to delete",
                DiscordLocale.ENGLISH_UK to "Amount of messages to delete",
                DiscordLocale.PORTUGUESE_BRAZILIAN to "Quantidade de mensagens para apagar"
            ),
            type = OptionType.INTEGER
        )
    )

    override val usage: String
        get() = "/$name <amount of messages(between 1 and 100)>"

    /**
     * Clears the specified amount of messages if possible.
     * @param context The [SlashCommandInteractionEvent].
     * @author Slz
     * @see canHandle
     */
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.member!!
        val channel = context.channel
        val amountOfMessages = context.options[0].asInt

        if (!canHandle(context)) return

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val adminSuccess: YamlMap = success["admin"]!!

        channel.iterableHistory.takeAsync(amountOfMessages).thenAccept(channel::purgeMessages)
        context.channel
            .sendMessage(String.format(adminSuccess.getText("clear_applied_public_message"), sender.asMention, amountOfMessages))
            .queue()
    }

    /**
     * Checks if it's possible to clear the specified amount of messages.
     * @param context The interaction to retrieve info, check and reply.
     * @return true if it's possible to delete the specified amount of messages, false otherwise.
     * @author Slz
     * @see execute
     */
    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val amountOfMessages = context.options[0].asInt
        val bot = context.guild!!.selfMember

        val err: YamlMap = getYML(sender.user).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val adminErr: YamlMap = err["admin"]!!

        if (amountOfMessages > 100 || amountOfMessages <= 0) {
            context.michiReply(adminErr.getText("clear_invalid_value"))
            return false
        }

        if (!sender.permissions.any(userPermissions::contains)) {
            context.michiReply(String.format(genericErr.getText("user_missing_perms"), Emoji.michiBlep))
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        return true
    }

}