package michi.bot.commands.admin

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.awt.Color

@Suppress("Unused")
object UnBan: MichiCommand("unban", "Unbans the mentioned users.", CommandScope.GUILD_SCOPE) {

    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR,
            Permission.BAN_MEMBERS
        )

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.BAN_MEMBERS,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/unban <1st user>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("user", "the 1st user to ban", OptionType.USER)
        )

    /**
     * Unbans the mentioned user(s) if possible.
     * @param context The interaction to retrieve info from.
     * @author Slz
     */
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val guild = context.guild!!

        // guard clauses
        if (!canHandle(context)) return

        val subject = context.getOption("user")!!.asUser

        // if everything is right
        guild.unban(subject).queue()

        EmbedBuilder().apply {
            setColor(Color.BLUE).setTitle("UNBAN! ${Emoji.michiHappy}")
            addField("Unbanned ${subject.name}", "", false)
            setFooter("It's better that this user don't cause any trouble again")

        }.build()
            .let(context::replyEmbeds)
            .queue()

        // puts the user that sent the command in cooldown
        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val agent = context.member!!
        val subject = context.getOption("user")?.asUser ?: return false
        val guild = context.guild!!
        val bot = guild.selfMember

        if (locateUserInGuild(guild, subject)) {
            context.reply("${subject.name} isn't banned ${Emoji.michiHuh}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!agent.permissions.any { permission -> userPermissions.contains(permission) }) {
            context.reply("You don't have the permissions to use this command, silly you ${Emoji.michiBlep}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}")
                .setEphemeral(true)
                .queue()
            return false
        }

        return true
    }

    /**
     * Searches for a member in the guild.
     * @param guild The guild to look for the user;
     * @param user The user to search on the guild.
     * @return True if the user was found in the guild, false if not.
     * @author Slz
     */
    private fun locateUserInGuild(guild: Guild, user: User): Boolean {
        var userNotFound = false

        guild.retrieveMember(user).queue(null) { userNotFound = true }
        return !userNotFound
    }

}