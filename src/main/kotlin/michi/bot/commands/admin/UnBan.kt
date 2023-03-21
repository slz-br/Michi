package michi.bot.commands.admin

import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

object UnBan {

    /**
     * Checks if the users to unban are actually banned.
     * @param context The slashCommandInteractionEvent that called the unban command.
     * @author Slz
     */
    fun tryToExecute(context: SlashCommandInteractionEvent) {
        val options = context.options
        val guild = context.guild!!
        val usersToUnban = mutableListOf<User>()

        for (subject in options) {

            if (subject.type.name != "USER") continue

            if (locateUserInGuild(guild, subject.asUser)) {
                context.reply("${subject.asUser.name} isn't banned ${Emoji.michiHuh}")
                    .setEphemeral(true)
                    .queue()
                return
            }
            usersToUnban.add(subject.asUser)
        }
        unban(context, *usersToUnban.toSet().toTypedArray())
    }

    /**
     * Unbans the mentioned user(s) if possible
     * @param context The SlashCommandInteractionEvent that called this function.
     * @param subjects The members to unban.
     * @author Slz
     */
    private fun unban(context: SlashCommandInteractionEvent, vararg subjects: User) {

        // this is asserted because in the SlashCommandListener it's tested if the command
        // comes or not from a guild
        val guild = context.guild!!

        // guard clauses
        if (!isPossible(context)) return

        // if everything is right
        val embed = EmbedBuilder()
        embed.setColor(Color.BLUE).setTitle("Unban! ${Emoji.michiHappy}")

        for (subject in subjects) {
            guild.unban(subject).queue()
            embed.addField("Unbanned ${subject.name}", "", false)
        }

        if (subjects.size == 1) embed.setFooter("It's better that this user don't cause any trouble again")
        else embed.setFooter("It's better that they don't cause any trouble again")

        context.replyEmbeds(embed.build()).queue()

    }

    /**
     * Checks if it is possible to unban the subjects.
     * @param context The SlashCommandInteractionEvent that called the unban command.
     * @return True if all members can be unbanned, false if not.
     * @author Slz
     */

    private fun isPossible(context: SlashCommandInteractionEvent): Boolean {
        val agent = context.member!!
        val canAgentPerformUnban = agent.hasPermission(Permission.BAN_MEMBERS) || agent.hasPermission(Permission.ADMINISTRATOR)

        // check if the agent has the permissions to use the command
        if (!canAgentPerformUnban) {
            context.reply("You don't have the permissions to use this command, silly you ${Emoji.michiBlep}")
                .setEphemeral(true)
                .queue()
            return false
        }

        return true
    }

    private fun locateUserInGuild(guild: Guild, user: User): Boolean {
        var userNotFound = false

        guild.retrieveMember(user).queue(null) { userNotFound = true }
        if (userNotFound) return false
        return true
    }
}