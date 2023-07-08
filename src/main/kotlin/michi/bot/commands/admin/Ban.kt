package michi.bot.commands.admin

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.config
import michi.bot.listeners.SlashCommandListener
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import michi.bot.util.Emoji
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button

/**
 * Object for the "ban" command, a command that bans the mentioned user from
 * the guild if possible(case the sender and the bot have permission to ban the user)
 * @author Slz
 */
@Suppress("Unused")
object Ban: MichiCommand("ban", "Bans the mentioned users.", CommandScope.GUILD_SCOPE) {

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
        get() = "/ban <user1> <user2(optional)> <user3(optional)> <reason(optional)>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("user", "the user to ban", OptionType.USER),
            MichiArgument("reason", "The reason for the ban.", OptionType.STRING, isRequired = false)
        )

    /**
     * Searches for a member in the guild.
     * @param guild The guild to look for the user;
     * @param user The user to search on the guild.
     * @return True if the user was found in the guild, false if not.
     * @author Slz
     */
    private fun locateUserInGuild(guild: Guild, user: User): Boolean {
        var userNotFound = false

        guild.members.any { it.user == user }
        guild.retrieveMember(user).queue(null) { userNotFound = true }
        return userNotFound
    }

    /**
     * Bans the mentioned member(s) if possible
     * @param context The SlashCommandInteractionEvent that called this function
     * @author Slz
     * @see canHandle
     */
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val subject = context.getOption("user")!!.asMember!!
        var reason = context.getOption("reason")?.asString

        // guard clause
        if (!canHandle(context)) return

        if (reason != null && reason.length > 1750) reason = null

        // ban confirmation
        context.reply("Are you sure you want to ban ${subject.asMention}?\nreason: ${reason ?: "not provided or too large"}")
            .setActionRow(Button.danger("ban-confirmation", "Ban!"))
            .queue()

        // puts the user that sent the command in cooldown
        SlashCommandListener.cooldownManager(sender)
    }

    /**
     * Checks if it's possible to ban the mentioned users.
     * @param context The interaction to retrieve info, check and reply.
     * @author Slz
     * @see execute
     */
    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val bot = context.guild!!.selfMember
        val subject = context.getOption("user")!!.asMember!!
        val senderTopRole = sender.roles.sortedDescending()[0].position
        val botTopRole = bot.roles.sortedDescending()[0].position

        if (locateUserInGuild(context.guild!!, subject.user)) {
            context.reply("Couldn't find the user in the guild ${Emoji.michiShrug}")
                .setEphemeral(true)
                .queue()
            return false
        }

        // checks if the agent is devil and is trying to ban michi >:(
        if (subject.id == config["BOT_ID"]) {
            context.reply("You can't ban me, idiot ${Emoji.michiUnimpressed}")
                .setEphemeral(true)
                .queue()
            return false
        }

        // checks if the agent is trying to ban himself
        if (subject == sender.user) {
            context.reply("Are you trying to ban yourself? ${Emoji.michiHuh}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!sender.permissions.any { permission -> userPermissions.contains(permission) }) {
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

        if (subject.roles.any { role -> role.position >= senderTopRole || role.position >= botTopRole }) {
            context.reply("${subject.asMention} has a greater role than you or me")
                .setEphemeral(true)
                .queue()
            return false
        }

        return true
    }

}
