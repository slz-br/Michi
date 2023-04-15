package michi.bot.commands.admin

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.config
import michi.bot.listeners.SlashCommandListener
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import michi.bot.util.Emoji
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.awt.Color
import java.util.concurrent.TimeUnit

/**
 * Object for the "ban" command, a command that bans mentioned users from
 * the server if possible(case the sender and the bot have permission to ban)
 * @author Slz
 */
object Ban: MichiCommand("ban", "Bans the mentioned users.", CommandScope.GUILD_SCOPE) {
    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR,
            Permission.BAN_MEMBERS
        )

    override val botPermisions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR,
            Permission.BAN_MEMBERS,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI
        )

    override val usage: String
        get() = "/ban <user1> <user2(optional)> <user3(optional)> <reason(optional)>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("user1", "the 1st user to ban", OptionType.USER, isRequired = true, hasAutoCompletion = false),
            MichiArgument("user2", "the 2nd user to ban", OptionType.USER, isRequired = false, hasAutoCompletion = false),
            MichiArgument("user3", "the 3rd user to ban", OptionType.USER, isRequired = false, hasAutoCompletion = false),
            MichiArgument("reason", "The reason for the ban.", OptionType.STRING, isRequired = false, hasAutoCompletion = false)
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
    @OptIn(DelicateCoroutinesApi::class)
    override fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val reason = context.getOptionsByName("reason")[0].asString
        val subjects = mutableListOf<Member>()

        // guard clause
        if (!canHandle(context)) return

        context.options.forEach { if (it.type == OptionType.USER) subjects.add(it.asMember!!) }

        // if everything is right:
        val embed = EmbedBuilder()
        embed.setColor(Color.RED).setTitle("**Ban!** " + Emoji.michiExcite)

        for (subject in subjects.toSet()) {
            subject.ban(1, TimeUnit.HOURS).queue()
            embed.addField("Banned ${subject.user.name}", "", false)
        }

        if (reason.isNotBlank() && reason.length < 1750) {
            embed.addField("Reason: ", reason, false)
        }

        context.replyEmbeds(embed.build()).queue()

        // puts the user that sent the command in cooldown
        GlobalScope.launch { SlashCommandListener.cooldownManager(sender) }
    }

    /**
     * Checks if it's possible to ban the mentioned users.
     * @param context The interaction to retrieve info, check and reply.
     * @author Slz
     * @see execute
     */
    override fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val bot = context.guild!!.selfMember
        val options = context.options
        val senderTopRole = sender.roles.sortedDescending()[0].position
        val botTopRole = context.guild!!.getMember(context.jda.selfUser)!!.roles.sortedDescending()[0].position
        val subjects = mutableListOf<User>()
        val subjectsAsMembers = mutableListOf<Member>()
        options.forEach { if (it.type == OptionType.USER) subjects.add(it.asUser) }

        if (subjects.any { locateUserInGuild(context.guild!!, it) }) {
            context.reply("Couldn't find the user in the guild ${Emoji.michiShrug}").setEphemeral(true).queue()
            return false
        }

        options.forEach {
            if (it.type == OptionType.USER) subjectsAsMembers.add(it.asMember!!)
        }

        // checks if the agent is devil and is trying to ban michi >:(
        if (subjects.any { it.id == config["BOT_ID"] }) {
            context.reply("You can't ban me, idiot ${Emoji.michiUnimpressed}").setEphemeral(true).queue()
            return false
        }

        // checks if the agent is trying to ban himself
        if (subjects.any { it == sender.user }) {
            context.reply("Are you trying to ban yourself? ${Emoji.michiHuh}").setEphemeral(true).queue()
            return false
        }

        if (!sender.permissions.any { permission -> userPermissions.contains(permission) }) {
            context.reply("You don't have the permissions to use this command, silly you ${Emoji.michiBlep}").setEphemeral(true).queue()
            return false
        }

        if (!bot.permissions.containsAll(botPermisions)) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}").setEphemeral(true).queue()
            return false
        }

        for (subject in subjectsAsMembers) {
            if (subject.roles.any { role -> role.position >= senderTopRole || role.position >= botTopRole }) {
                context.reply("${subject.asMention} has a greater role than you or me").setEphemeral(true).queue()
                return false
            }
        }

        return true
    }

}
