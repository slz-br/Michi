package michi.bot.commands.admin

import michi.bot.config
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import michi.bot.util.Emoji
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import java.util.concurrent.TimeUnit

object Ban {

    /**
     * Checks if all users to ban are in the guild.
     * @param context the slashCommandInteractionEvent that called the ban command.
     * @author Slz
     */
    fun tryToExecute(context: SlashCommandInteractionEvent) {
        val options = context.options
        val guild = context.guild!!
        val subjects = mutableListOf<Member>()
        var reason: String? = null

        for (option in options) {
            if (option.type.name != "USER") continue
            if (option.type.name == "STRING") reason = option.asString

            if (!locateUserInGuild(guild, option.asUser)) {
                context.reply("couldn't find ${option.asUser.name} in the guild")
                    .setEphemeral(true)
                    .queue()
                return
            }

            subjects.add(option.asMember!!)
        }
        ban(context, reason, *subjects.toSet().toTypedArray())
    }

    /**
     * Bans the mentioned member(s) if possible
     * @param context The SlashCommandInteractionEvent that called this function
     * @param reason The reason of the ban.
     * @param subjects The members to ban.
     * @author Slz
     */
    private fun ban(context: SlashCommandInteractionEvent, reason: String?, vararg subjects: Member) {

        // guard clause
        if (!isPossible(context, subjects.toSet())) return

        // if everything is right:
        val embed = EmbedBuilder()
        embed.setColor(Color.RED).setTitle("**Ban!** " + Emoji.michiExcite)

        for (subject in subjects.toSet()) {
            subject.ban(1, TimeUnit.HOURS).queue()
            embed.addField("Banned ${subject.user.name}", "", false)
        }

        if (reason != null) {
            embed.addField("Reason: ", "$reason", false)
        }

        context.replyEmbeds(embed.build())
            .queue()
    }

    /**
     * Checks if it is possible to ban the subjects
     * @param context The SlashCommandInteraction that called the ban function
     * @param subjects Members to check if they can be banned.
     * @return True if all members can be banned, false if not.
     * @author Slz
     */
    private fun isPossible(context: SlashCommandInteractionEvent, subjects: Set<Member>): Boolean {

        val agent = context.member!!
        val agentTopRole = agent.roles.sortedDescending()[0].position
        val botTopRole = context.guild!!.getMember(context.jda.selfUser)!!.roles.sortedDescending()[0].position

        // checks if the agent has the permission to ban
        if (!agent.hasPermission(Permission.BAN_MEMBERS) && !agent.hasPermission(Permission.ADMINISTRATOR)) {
            context.reply("You don't have the permissions to use this command.")
                .setEphemeral(true)
                .queue()
            return false
        }

        // checks if the agent is devil and is trying to ban michi >:(
        if (subjects.any { it.id == config.get("BOT_ID") }) {
            context.reply("You can't ban me, idiot ${Emoji.michiUnimpressed}")
                .setEphemeral(true)
                .queue()
            return false
        }

        // checks if the agent is trying to ban himself
        if (subjects.any { it.user == agent.user }) {
            context.reply("Are you trying to ban yourself? ${Emoji.michiHuh}")
                .setEphemeral(true)
                .queue()
            return false
        }

        for (subject in subjects) {

            // checks if one of the subjects have a role that is hierarchically greater than the agent or the bot
            if (subject.roles.any { role -> role.position >= agentTopRole || role.position >= botTopRole }) {
                context.reply("A user that you are trying to ban has a greater role than you or me")
                    .setEphemeral(true)
                    .queue()
                return false
            }

            var hadError = false
            context.guild!!.retrieveBan(subject.user).queue {
                hadError = true
                context.reply("A user that you are trying to ban is already banned.")
                    .setEphemeral(true)
                    .queue()
            }

            if (hadError) return false

        }

        return true
    }

    /**
     * Searchs for a member in the guild.
     * @param guild The guild to look for the user;
     * @param user The user to search on the guild.
     * @return True if the user was found in the guild, false if not.
     * @author Slz
     */
    private fun locateUserInGuild(guild: Guild, user: User): Boolean {
        var userNotFound = false

        guild.retrieveMember(user).queue(null) { userNotFound = true }
        if (userNotFound) return false
        return true
    }

}
