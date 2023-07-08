package michi.bot.commands.mail

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

@Suppress("Unused")
object RemoveMail: MichiCommand("remove-mail", "Removes a mail at a specific position from your inbox.", CommandScope.GLOBAL_SCOPE) {

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_SEND_IN_THREADS
        )
    override val usage: String
        get() = "/remove-mail <position(the position of the mail in your inbox)>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("position", "The position of the mail to remove", OptionType.INTEGER, isRequired = true, hasAutoCompletion = false)
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user

        if (!canHandle(context)) return

        val mailIndex = context.getOption("position")!!.asInt - 1

        inboxMap[sender] ?: inboxMap.computeIfAbsent(sender) {
            val userInbox = arrayListOf<MailMessage>()
            userInbox
        }.removeAt(mailIndex)

        context.reply("Removed the mail at position ${mailIndex + 1} from your inbox")
            .setEphemeral(true)
            .queue()

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val mailIndex = context.getOption("position")!!.asInt.minus(1)
        val guild = context.guild
        val inbox = inboxMap[sender] ?: inboxMap.computeIfAbsent(sender) {
            val userInbox = arrayListOf<MailMessage>()
            userInbox
        }

        if (inbox.isEmpty()) {
            context.reply("Your inbox is empty ${Emoji.michiSad}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (mailIndex >= inbox.size - 1 || mailIndex < 0) {
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