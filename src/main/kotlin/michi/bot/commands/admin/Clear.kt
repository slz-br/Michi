package michi.bot.commands.admin

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

object Clear: MichiCommand("clear", "Deletes a certain amount of messages from this chat.", CommandScope.GUILD_SCOPE) {
    override val userPermissions: List<Permission>
        get() = listOf(Permission.ADMINISTRATOR, Permission.MESSAGE_MANAGE)

    override val botPermisions: List<Permission>
        get() = listOf(Permission.ADMINISTRATOR, Permission.MESSAGE_MANAGE, Permission.MESSAGE_SEND)

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("amount", "the amount of messages to delete", OptionType.INTEGER, true)
        )

    override val usage: String
        get() = "/clear <amount of messages(between 1 and 100)>"

    @OptIn(DelicateCoroutinesApi::class)
    override fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.member!!
        val channel = context.channel.asTextChannel()
        val amountOfMessages = context.options[0].asInt

        if (!canHandle(context)) return

        channel.iterableHistory.takeAsync(amountOfMessages).thenAccept(channel::purgeMessages)

        context.channel.asTextChannel().sendMessage("${sender.asMention} cleared $amountOfMessages messages in this chat.")
        context.reply("Chat successfully cleared").setEphemeral(true).queue()

        // puts the user that sent the command in cooldown
        GlobalScope.launch { SlashCommandListener.cooldownManager(sender.user) }
    }

    override fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val amountOfMessages = context.options[0].asInt
        val bot = context.guild!!.selfMember

        if (amountOfMessages > 100 || amountOfMessages <= 0) {
            context.reply("Invalid value(must be between 1 and 100)").setEphemeral(true).queue()
            return false
        }

        if (!sender.permissions.any{ permission -> userPermissions.contains(permission) }) {
            context.reply("You don't have the permissions to use this command, silly you ${Emoji.michiBlep}").setEphemeral(true).queue()
            return false
        }

        if (!bot.permissions.any{ permission -> botPermisions.contains(permission) }) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}").setEphemeral(true).queue()
            return false
        }

        return true
    }

}