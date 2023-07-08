package michi.bot.commands.admin

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.time.Duration

@Suppress("Unused")
object Mute: MichiCommand("mute", "Mutes the mentioned member for a specific period of time.", CommandScope.GUILD_SCOPE) {
    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR,
            Permission.MODERATE_MEMBERS
        )

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MODERATE_MEMBERS,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/mute <user> <how much time>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument(
                "user",
                "The user to mute",
                OptionType.USER, isRequired = true,
                hasAutoCompletion = false
            ),
            MichiArgument(
                "time",
                "The time to mute the user",
                OptionType.STRING,
                isRequired = true,
                hasAutoCompletion = true
            )
        )

    /**
     * Mute the mentioned user(s) if possible.
     * @param context The Interaction to reply to.
     * @author Slz
     * @see canHandle
     */
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user

        if (!canHandle(context)) return

        val time = context.options[1].asString
        val memberToMute = context.options[0].asMember!!

        when (time) {
            "1 min" ->  {
                memberToMute.timeoutFor(Duration.ofMinutes(1)).queue()
                context.reply("${sender.asMention} muted ${memberToMute.asMention} for 1 minute")
                    .queue()
            }
            "5 min" ->  {
                memberToMute.timeoutFor(Duration.ofMinutes(5)).queue()
                context.reply("${sender.asMention} muted ${memberToMute.asMention} for 5 minutes")
                    .queue()
            }
            "10 min" -> {
                memberToMute.timeoutFor(Duration.ofMinutes(10)).queue()
                context.reply("${sender.asMention} muted ${memberToMute.asMention} for 10 minutes")
                    .queue()
            }
            "1 hour" -> {
                memberToMute.timeoutFor(Duration.ofHours(1)).queue()
                context.reply("${sender.asMention} muted ${memberToMute.asMention} for 1 hour")
                    .queue()
            }
            "1 day"  -> {
                memberToMute.timeoutFor(Duration.ofDays(1)).queue()
                context.reply("${sender.asMention} muted ${memberToMute.asMention} for 1 day")
                    .queue()
            }
            "1 week" -> {
                memberToMute.timeoutFor(Duration.ofDays(7)).queue()
                context.reply("${sender.asMention} muted ${memberToMute.asMention} for 1 week")
                    .queue()
            }
            else -> {
                context.reply("Type a valid option ${Emoji.smolMichiAngry}")
                    .setEphemeral(true)
                    .queue()
                return
            }
        }

        // puts the user that sent the command in cooldown
        SlashCommandListener.cooldownManager(sender)
    }

    /**
     * Checks if it is possible to ban the subjects.
     * @param context The SlashCommandInteraction that called the ban function.
     * @return True if all members can be banned, false if not.
     * @author Slz
     * @see execute
     */
    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild!!
        val sender = context.member!!
        val bot = guild.selfMember
        val memberToMute = context.options[0].asMember
        val senderTopRole = sender.roles.sortedDescending()[0].position
        val botTopRole = bot.roles.sortedDescending()[0].position

        if (memberToMute?.user == sender) {
            context.reply("Why are you trying to mute yourself? ${Emoji.michiHMPH}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (memberToMute == bot) {
            context.reply("Stop trying to mute me! ${Emoji.smolMichiAngry}")
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

        memberToMute?.let {

        if (!memberToMute.permissions.any { permission -> userPermissions.contains(permission) }) {
            context.reply("You don't have permission to use this command, silly you ${Emoji.michiBlep}")
                .setEphemeral(true)
                .queue()
            return false
        }


            if (memberToMute.roles.any { role -> role.position >= senderTopRole || role.position >= botTopRole }) {
                context.reply("${memberToMute.asMention} has a greater role than you or me")
                    .setEphemeral(true)
                    .queue()
                return false
            }

            return true
        }

        context.reply("Couldn't find the user in the guild")
            .setEphemeral(true)
            .queue()
        return false
    }

}