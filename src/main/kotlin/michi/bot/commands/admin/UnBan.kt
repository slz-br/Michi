package michi.bot.commands.admin

import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

/**
 * Unbans the mentioned user(s) if possible
 * @param context The SlashCommandInteractionEvent that called this function.
 * @param subjects The members to unban.
 * @author Slz
 */
fun unban(context: SlashCommandInteractionEvent, vararg subjects: User) {

    // this is asserted because in the SlashCommandListener it's tested if the command
    // comes or not from a guild
    val guild = context.guild

    // guard clauses
    if (!isPossible(context, subjects.toSet())) return
    guild ?: return

    // if everything is right
    val embed = EmbedBuilder()
    embed.setColor(Color.BLUE).
    setTitle("Unban! ${Emoji.michiHappy}")

    for (subject in subjects) {
        guild.unban(subject).queue()
        embed.addField("Unbanned ${subject.name}","", false)
    }

    if (subjects.size == 1) {
        embed.setFooter("It's better that this user don't cause any trouble again")
        context.replyEmbeds(embed.build())
    }
    else {
        embed.setFooter("It's better that they don't cause any trouble again")
    }

    context.replyEmbeds(embed.build()).queue()

}

/**
 * Checks if it is possible to unban the subjects.
 * @param context The SlashCommandInteractionEvent that called the unban command.
 * @param subjects The members to check if they can be unbanned
 * @return True if all members can be unbanned, false if not.
 * @author Slz
 */

private fun isPossible(context: SlashCommandInteractionEvent, subjects: Set<User>): Boolean {
    val agent = context.member!!
    val guild = context.guild!!
    var userNotBanned = false

    if (subjects.size > 5) {
        context.reply("You can't unban more than 5 people at the same time with this command.")
            .setEphemeral(true)
            .queue()
        return false
    }

    for (subject in subjects) {

        // check if the agent has the permissions to use the command
        if (!agent.hasPermission(Permission.BAN_MEMBERS) && !agent.hasPermission(Permission.ADMINISTRATOR)) {
            context.reply("You don't have the permissions to use this command.")
                .setEphemeral(true)
                .queue()
            return false
        }

        // checks if the subject is already banned
        guild.retrieveBan(subject).queue(null) { userNotBanned = true }
        if (userNotBanned) {
            context.reply("A user isn't banned")
                .setEphemeral(true)
                .queue()
            return false
        }

    }

    return true
}