package michi.bot.commands.admin

import kotlinx.coroutines.*
import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

@Suppress("Unused")
object SlowMode: MichiCommand("slowmode", "Sets the channel slowmode.", CommandScope.GUILD_SCOPE) {

    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR,
            Permission.MANAGE_CHANNEL
        )

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MANAGE_CHANNEL,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/slowmode <time>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("time", "the slowmode time.", OptionType.STRING, isRequired = true, hasAutoCompletion = true)
        )

    /**
     * Applies slowMode to the channel that the command was sent in.
     * @param context The interaction to reply to.
     * @author Slz
     */
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.member!!
        val channel = context.channel.asTextChannel()

        if (!canHandle(context)) return

        val slowTime = context.getOption("time")!!.asString
        val manager = channel.manager

        when (slowTime) {
            "0"  -> {
                if (channel.slowmode == 0) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(0).queue()
            }

            "5s" -> {
                if (channel.slowmode == 5) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(5).queue()
            }

            "10s" -> {
                if (channel.slowmode == 10) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(10).queue()
            }

            "15s" -> {
                if (channel.slowmode == 15) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(15).queue()
            }

            "30s" -> {
                if (channel.slowmode == 30) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(30).queue()
            }

            "1m" -> {
                if (channel.slowmode == 60) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(60).queue()
            }

            "2m" -> {
                if (channel.slowmode == 120) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(120).queue()
            }

            "5m" -> {
                if (channel.slowmode == 300) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(300).queue()
            }

            "10m" -> {
                if (channel.slowmode == 600) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(600).queue()
            }

            "15m" -> {
                if (channel.slowmode == 900) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(900).queue()
            }

            "30m" -> {
                if (channel.slowmode == 1800) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(1800).queue()
            }

            "1h" -> {
                if (channel.slowmode == 3600) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(3600).queue()
            }

            "2h" -> {
                if (channel.slowmode == 7200) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(7200).queue()
            }

            "6h" -> {
                if (channel.slowmode == ISlowmodeChannel.MAX_SLOWMODE) {
                    context.reply("This is the channel slowmode time already.")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                manager.setSlowmode(ISlowmodeChannel.MAX_SLOWMODE).queue()
            }

            else -> {
                context.reply("Type a valid option ${Emoji.smolMichiAngry}")
                    .setEphemeral(true)
                    .queue()
                return
            }
        }

        context.reply("SlowMode successfully applied to ${channel.asMention}")
            .setEphemeral(true)
            .queue()

        if (slowTime == "0") channel.sendMessage("${sender.asMention} removed the slowmode from this channel ${Emoji.michiJoy}").queue()
        else channel.sendMessage("${sender.asMention} slowmoded this channel.").queue()

        // puts the user that sent the command in cooldown
        CoroutineScope(Dispatchers.IO).launch { SlashCommandListener.cooldownManager(sender.user) }
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member ?: return false
        val guild = context.guild ?: return false
        val slowTime = context.getOption("time")!!.asString
        val bot = guild.selfMember
        val channel = context.channel.asTextChannel()

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

        if (slowTime == "0" && channel.slowmode == 0) {
            context.reply("The channel already isn't slowmoded.")
                .setEphemeral(true)
                .queue()
            return false
        }

        return true
    }

}