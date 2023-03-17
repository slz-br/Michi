package michi.bot.commands.admin

import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object SlowMode {

    /**
     * Checks if the user that sent the command has "administrator" or "manage_channels" permission
     * @param context The interaction to check
     * @author Slz
     */
    fun tryToExecute(context: SlashCommandInteractionEvent) {
        val sender = context.member!!

        if (!sender.hasPermission(Permission.ADMINISTRATOR) || !sender.hasPermission(Permission.MANAGE_CHANNEL)) {
            context.reply("You don't have the permissions to use this command, silly you ${Emoji.michiBlep}")
                .setEphemeral(true)
                .queue()
            return
        }

        slowMode(context)
    }

    /**
     * Applies slowMode to the channel that the command was sent in.
     * @param context The interaction to reply to.
     * @author Slz
     */
    private fun slowMode(context: SlashCommandInteractionEvent) {
        val sender = context.member!!
        val channel = context.channel.asTextChannel()
        val slowTime = context.options[0].asInt

        if (slowTime > 21600 || slowTime < 0) {
            context.reply("Invalid value(can't be less than 0 or greater than 21600).")
                .setEphemeral(true)
                .queue()
            return
        }

        channel.manager.setSlowmode(slowTime).queue {
            context.reply("SlowMode successfully applied to ${channel.asMention}")
                .setEphemeral(true)
                .queue()
            if (slowTime == 0) channel.sendMessage("${sender.asMention} removed the slowmode from this channel ${Emoji.michiJoy}")
                .queue()
            else channel.sendMessage("${sender.asMention} slowmoded this channel").queue()
        }
    }

}