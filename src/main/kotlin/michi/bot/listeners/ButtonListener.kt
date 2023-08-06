package michi.bot.listeners

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color
import java.util.concurrent.TimeUnit

import michi.bot.commands.mail.inboxMap
import michi.bot.config
import michi.bot.database.dao.GuildsDAO
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
import michi.bot.util.Language
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML

/**
 * Class that holds the event handler [onButtonInteraction].
 * @author Slz
 */
object ButtonListener: ListenerAdapter() {

    /**
     * Called whenever a [ButtonInteractionEvent] happens.
     * @author Slz
     */
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        CoroutineScope(IO).launch {
            val message = event.message
            val messageEmbeds = message.embeds

            when (event.button.id) {

                "ban-confirmation" -> {
                    val userToBan = message.mentions.users[0]
                    val reason = message.contentRaw.split("reason: ")[1]
                    val author = event.user
                    val allowedUser = message.interaction!!.user

                    if (author != allowedUser) {
                        val err: YamlMap = getYML(Language.EN_US).yamlMap["error_messages"]!!
                        val genericErr: YamlMap = err["generic"]!!

                        event.reply(String.format(genericErr.getText("unauthorized_user_interaction"), Emoji.michiGlare))
                            .setEphemeral(true)
                            .queue()
                        return@launch
                    }

                    event.guild!!.ban(userToBan, 1, TimeUnit.HOURS).reason(reason).queue()

                    message.delete().queue()

                    val success: YamlMap = getYML(Language.EN_US).yamlMap["success_messages"]!!
                    val adminSuccess: YamlMap = success["admin"]!!
                    val banMessage = adminSuccess.getText("ban_applied").split("\n")

                    val embed = EmbedBuilder().apply {
                        setColor(Color.RED).setTitle(String.format(banMessage[0], Emoji.michiExcite))

                        addField(String.format(banMessage[1], userToBan.asMention), String.format(banMessage[2], event.user.asMention), false)

                        if (reason.isNotBlank() && reason.length <= 1750) addField(banMessage[3], reason, false)
                    }

                    event.replyEmbeds(embed.build()).queue()
                }

                "clear-queue-confirmation" -> {
                    message.delete().queue()

                    val guild = event.guild!!
                    val botVoiceChannel = guild.selfMember.voiceState!!.channel?.asGuildMessageChannel()
                    val trackQueue = PlayerManager[guild].scheduler.trackQueue
                    val author = event.user
                    val allowedUser = message.interaction!!.user

                    if (author != allowedUser) {
                        val err: YamlMap = getYML(Language.EN_US).yamlMap["error_messages"]!!
                        val genericErr: YamlMap = err["generic"]!!

                        event.reply(String.format(genericErr.getText("unauthorized_user_interaction"), Emoji.michiGlare))
                            .setEphemeral(true)
                            .queue()
                        return@launch
                    }

                    val success: YamlMap = getYML(Language.EN_US).yamlMap["success_messages"]!!
                    val musicDJSuccess: YamlMap = success["music_dj"]!!

                    trackQueue.clear()

                    GuildsDAO.setMusicQueue(guild, "")

                    event.reply(String.format(musicDJSuccess.getText("clear_queue_ephemeral_message"), Emoji.michiThumbsUp))
                        .setEphemeral(true)
                        .queue()
                    botVoiceChannel?.sendMessage(String.format(musicDJSuccess.getText("clear_queue_public_message"), author.asMention))
                        ?.queue()
                }

                "clear-queue-cancel" -> {
                    message.delete().queue()

                    val success: YamlMap = getYML(Language.EN_US).yamlMap["success_messages"]!!
                    val musicDJSuccess: YamlMap = success["music_dj"]!!
                    event.reply(String.format(musicDJSuccess.getText("clear_queue_cancelled"), Emoji.michiThumbsUp))
                        .queue()
                }

                "clear-mail-inbox-confirmation" -> {
                    message.delete().queue()

                    val success: YamlMap = getYML(Language.EN_US).yamlMap["success_messages"]!!
                    val mailSuccess: YamlMap = success["mail"]!!
                    event.reply(String.format(mailSuccess.getText("inbox_cleared"), Emoji.michiThumbsUp))
                        .setEphemeral(true)
                        .queue()
                    inboxMap[event.user]!!.clear()
                    return@launch
                }

                "cancel-mail-inbox-clearing" -> {
                    message.delete().queue()

                    val success: YamlMap = getYML(Language.EN_US).yamlMap["success_messages"]!!
                    val mailSuccess: YamlMap = success["mail"]!!
                    event.reply(String.format(mailSuccess.getText("clear_inbox_cancelled"), Emoji.michiThumbsUp))
                        .setEphemeral(true)
                        .queue()
                    return@launch
                }

                "report-confirmation" -> {
                    message.delete().queue()

                    val user = event.user
                    val reportChannel = event.jda.getGuildById(config["BOT_SERVER_ID"])!!.getTextChannelById("1085275324859818025")!!
                    val userInbox = inboxMap[user]!!
                    val mailIndex = messageEmbeds[0].fields[0].value!!.split(" ")[0].removePrefix("#").toInt() - 1

                    val mailToReport = userInbox[mailIndex]

                    reportChannel.sendMessage("$mailToReport\nsender: ${mailToReport.sender.asMention}\nreceiver: ${user.asMention}\nthis is a manual report.")
                        .queue()

                    val success: YamlMap = getYML(Language.EN_US).yamlMap["success_messages"]!!
                    val mailSuccess: YamlMap = success["mail"]!!

                    event.reply(String.format(mailSuccess.getText("mail_reported"), Emoji.michiThumbsUp))
                        .setEphemeral(true)
                        .queue()

                    userInbox -= mailToReport
                }

                "cancel-report" -> {
                    message.delete().queue()

                    val success: YamlMap = getYML(event).yamlMap["success_messages"]!!
                    val mailSuccess: YamlMap = success["mail"]!!

                    if (event.message.components.isNotEmpty()) {
                        event.reply(mailSuccess.getText("mail_report_cancelled"))
                            .setEphemeral(true)
                            .queue()
                        return@launch
                    }
                }

                "read-anyway" -> {
                    message.delete().queue()

                    val user = event.user
                    val mailIndex = messageEmbeds[0].fields[0].value!!.split("#")[1][0].digitToInt() - 1

                    val inbox = inboxMap[user]!!

                    event.reply(inbox[mailIndex].toString())
                        .setEphemeral(true)
                        .queue()
                }

                "cancel-reading" -> {
                    message.delete().queue()

                    val success: YamlMap = getYML(event).yamlMap["success_messages"]!!
                    val mailSuccess: YamlMap = success["mail"]!!

                    event.reply(mailSuccess.getText("read_cancel"))
                        .setEphemeral(true)
                        .queue()
                }

            }
        }
    }

}