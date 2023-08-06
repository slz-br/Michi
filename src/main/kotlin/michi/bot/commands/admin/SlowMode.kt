package michi.bot.commands.admin

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.listeners.CommandAutoCompletionListener
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * Object for the slowmode command, a command that defines a delay for sending messages
 * in a channel.
 * @author Slz
 */
@Suppress("Unused")
object SlowMode: MichiCommand("slowmode", GUILD_SCOPE) {

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
        get() = "/$name <time>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("time", OptionType.STRING, hasAutoCompletion = true)
        )

    /**
     * Applies slowMode to the channel that the command was sent in.
     * @param context The interaction to michiReply to.
     * @author Slz
     */
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.member!!

        if (!canHandle(context)) return

        val channel = context.channel.asTextChannel()

        val errMsg: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val adminErr: YamlMap = errMsg["admin"]!!

        val slowTime = context.getOption("time")!!.asString
        val manager = channel.manager

        when (slowTime) {
            "0"  -> {
                if (channel.slowmode == 0) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(0)
            }

            "5s" -> {
                if (channel.slowmode == 5) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(5)
            }

            "10s" -> {
                if (channel.slowmode == 10) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(10)
            }

            "15s" -> {
                if (channel.slowmode == 15) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(15)
            }

            "30s" -> {
                if (channel.slowmode == 30) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(30)
            }

            "1m" -> {
                if (channel.slowmode == 60) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(60)
            }

            "2m" -> {
                if (channel.slowmode == 120) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(120)
            }

            "5m" -> {
                if (channel.slowmode == 300) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(300)
            }

            "10m" -> {
                if (channel.slowmode == 600) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(600)
            }

            "15m" -> {
                if (channel.slowmode == 900) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(900)
            }

            "30m" -> {
                if (channel.slowmode == 1800) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(1800)
            }

            "1h" -> {
                if (channel.slowmode == 3600) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(3600)
            }

            "2h" -> {
                if (channel.slowmode == 7200) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(7200)
            }

            "6h" -> {
                if (channel.slowmode == ISlowmodeChannel.MAX_SLOWMODE) {
                    context.michiReply(adminErr.getText("slowmode_time_unchanged"))
                    return
                }
                manager.setSlowmode(ISlowmodeChannel.MAX_SLOWMODE)
            }

        }

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val adminSuccess: YamlMap = success["admin"]!!

        context.michiReply(adminSuccess.getText(String.format(adminSuccess.getText("slowmode_applied_ephemeral_message"), channel.asMention)))

        if (slowTime == "0") channel.sendMessage(String.format(adminSuccess.getText("slowmode_removed"), sender.asMention, Emoji.michiJoy)).queue()
        else channel.sendMessage(String.format(adminSuccess.getText("slowmode_applied_public_message"), sender.asMention)).queue()
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member ?: return false
        val guild = context.guild ?: return false
        val slowTime = context.getOption("time")!!.asString
        val bot = guild.selfMember
        val channel = context.channel

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val adminErr: YamlMap = err["admin"]!!

        if (slowTime !in CommandAutoCompletionListener.slowmodeAutoCompletion) {
            context.michiReply(String.format(genericErr.getText("option_err"), Emoji.smolMichiAngry))
            return false
        }

        if (!sender.permissions.any(userPermissions::contains)) {
            context.michiReply(String.format(genericErr.getText("user_missing_perms"), Emoji.michiBlep))
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        if (channel !is TextChannel) {
            context.michiReply(String.format(adminErr.getText("channel_type_err"), channel.type.name))
            return false
        }

        return true
    }

}