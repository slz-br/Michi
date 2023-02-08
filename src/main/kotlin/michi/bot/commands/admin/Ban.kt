package michi.bot.commands.admin

import michi.bot.config
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import michi.bot.util.Emoji
import java.awt.Color
import java.util.concurrent.TimeUnit

fun ban(context: SlashCommandInteractionEvent, reason: String?, vararg subjects: Member) {

    // guard clause
    if (!isPossible(context, subjects.toSet())) return

    // if everything is right:
    val embed = EmbedBuilder()
    embed.setColor(Color.RED).
    setTitle("**Ban!** " + Emoji.michiExcite)

    for (subject in subjects.toSet()) {
        subject.ban(5, TimeUnit.MINUTES).queue()
        embed.addField("Successfully banned ${subject.user.name}", "", false)
    }

    if (reason != null) {
        embed.addField("Reason: ", "$reason", false)
    }

    context.replyEmbeds(embed.build())
        .queue()
}

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

    // checks if the agent is trying to ban more than 5 people
    if (subjects.size > 5) {
        context.reply("You can't ban more than 5 people at the same time with this command.")
            .setEphemeral(true)
            .queue()
        return false
    }

    // checks if the agent is devil and is trying to ban michi >:(
    if (subjects.any { it.id == config.get("ID")}) {
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
        context.guild!!.retrieveBan(subject.user).queue{
            hadError = true
            context.reply("A user that you are trying to ban is already banned.")
                .setEphemeral(true)
                .queue()
        }

        if (hadError) return false

    }

    return true
}