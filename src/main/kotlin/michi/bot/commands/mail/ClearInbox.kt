package michi.bot.commands.mail

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

@Suppress("Unused")
object ClearInbox: MichiCommand("clear-inbox", "Clears all mails from your inbox", CommandScope.GLOBAL_SCOPE) {

    override val usage: String
        get() = "/clear-inbox"

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user

        if (!canHandle(context)) return

        val deleteButton = Button.danger("clear-mail-inbox-confirmation", "Delete")
        val cancelDeletion = Button.secondary("cancel-mail-inbox-clearing", "Cancel")

        context.reply("Are you sure you want to clear your inbox?").apply {
            setActionRow(deleteButton, cancelDeletion)
            setEphemeral(true)
        }.queue()

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val guild = context.guild

        val inbox = inboxMap[sender] ?: inboxMap.computeIfAbsent(sender) {
            val userInbox = arrayListOf<MailMessage>()
            userInbox
        }

        if (inbox.isEmpty()) {
            context.reply("There isn't anything to clear, your inbox is empty ${Emoji.michiSad}").setEphemeral(true).queue()
            return false
        }

        guild?.let {
            val bot = guild.selfMember
            if (!bot.permissions.containsAll(botPermissions)) {
                context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}").setEphemeral(true).queue()
                return false
            }
        }

        return true
    }


}