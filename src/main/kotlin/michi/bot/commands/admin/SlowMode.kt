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

object SlowMode: MichiCommand("slowmode", "Sets the channel slowmode.", CommandScope.GUILD_SCOPE) {

    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR,
            Permission.MANAGE_CHANNEL
        )

    override val botPermisions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR,
            Permission.MANAGE_CHANNEL,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI
        )

    override val usage: String
        get() = "/slowmode <time in seconds(between 0 and 21600)>"
    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("time", "the slowmode time in seconds.", OptionType.INTEGER, isRequired = true, hasAutoCompletion = false)
        )

    /**
     * Applies slowMode to the channel that the command was sent in.
     * @param context The interaction to reply to.
     * @author Slz
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.member!!
        val channel = context.channel.asTextChannel()
        val slowTime = context.options[0].asInt

        if (!canHandle(context)) return

        channel.manager.setSlowmode(slowTime).queue( {
            context.reply("SlowMode successfully applied to ${channel.asMention}").setEphemeral(true).queue()

            if (channel.slowmode == 0) channel.sendMessage("${sender.asMention} removed the slowmode from this channel ${Emoji.michiJoy}").queue()
            else channel.sendMessage("${sender.asMention} slowmoded this channel.").queue()

            // puts the user that sent the command in cooldown
            GlobalScope.launch { SlashCommandListener.cooldownManager(sender.user) }
            },
            { context.reply("Something went really wrong ${Emoji.michiOpsie}\nOpen a thread with the \"bug\" tag in my server: https://discord.gg/xDJeQ4xJ").setEphemeral(true).queue() }
        )
    }

    override fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val bot = context.guild!!.selfMember
        val channel = context.channel.asTextChannel()
        val slowTime = context.options[0].asInt

        if (!sender.permissions.any { permission -> userPermissions.contains(permission) }) {
            context.reply("You don't have the permissions to use this command, silly you ${Emoji.michiBlep}").setEphemeral(true).queue()
            return false
        }

        if (!bot.permissions.containsAll(botPermisions)) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}").setEphemeral(true).queue()
            return false
        }

        if (slowTime > 21600 || slowTime < 0) {
            context.reply("Invalid value(can't be less than 0 or greater than 21600).").setEphemeral(true).queue()
            return false
        }

        if (slowTime == 0 && channel.slowmode == 0) {
            context.reply("The channel already isn't slowmoded.").setEphemeral(true).queue()
            return false
        }

        return true
    }

}