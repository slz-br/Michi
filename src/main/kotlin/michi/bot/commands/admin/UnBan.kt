package michi.bot.commands.admin

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

object UnBan: MichiCommand("unban", "Unbans the mentioned users.", CommandScope.GUILD_SCOPE) {
    override val userPermissions: List<Permission>
        get() = listOf(Permission.ADMINISTRATOR, Permission.BAN_MEMBERS)
    override val botPermisions: List<Permission>
        get() = listOf(Permission.ADMINISTRATOR, Permission.BAN_MEMBERS, Permission.MESSAGE_SEND)
    override val usage: String
        get() = "/ban <1st user> <2nd user(optional) <3rd user(optional) <reason(optional)>"
    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("user1", "the 1st user to ban", OptionType.USER, true),
            MichiArgument("user2", "the 2nd user to ban", OptionType.USER, false),
            MichiArgument("user3", "the 3rd user to ban", OptionType.USER, false)
        )

    /**
     * Unbans the mentioned user(s) if possible
     * @param context The interaction to retrieve info from.
     * @author Slz
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val subjects = mutableListOf<User>()
        context.options.forEach { subjects.add(it.asUser) }

        // this is asserted because in the SlashCommandListener it's tested if the command
        // comes or not from a guild
        val guild = context.guild!!

        // guard clauses
        if (!canHandle(context)) return

        // if everything is right
        val embed = EmbedBuilder()
        embed.setColor(Color.BLUE).setTitle("UNBAN! ${Emoji.michiHappy}")

        for (subject in subjects) {
            guild.unban(subject).queue()
            embed.addField("Unbanned ${subject.name}", "", false)
        }

        if (subjects.size == 1) embed.setFooter("It's better that this user don't cause any trouble again")
        else embed.setFooter("It's better that they don't cause any trouble again")

        context.replyEmbeds(embed.build()).queue()

        // puts the user that sent the command in cooldown
        GlobalScope.launch { SlashCommandListener.cooldownManager(sender) }
    }

    override fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val agent = context.member!!
        val options = context.options
        val guild = context.guild!!
        val bot = guild.selfMember

        for (subject in options) {

            if (subject.type != OptionType.USER) continue

            if (locateUserInGuild(guild, subject.asUser)) {
                context.reply("${subject.asUser.name} isn't banned ${Emoji.michiHuh}")
                    .setEphemeral(true)
                    .queue()
                return false
            }

            if (!agent.permissions.any { permission -> userPermissions.contains(permission) }) {
                context.reply("You don't have the permissions to use this command, silly you ${Emoji.michiBlep}").setEphemeral(true).queue()
                return false
            }

            if (!bot.permissions.any{ permission -> Clear.botPermisions.contains(permission) }) {
                context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}").setEphemeral(true).queue()
                return false
            }

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
        if (userNotFound) return false
        return true
    }

}