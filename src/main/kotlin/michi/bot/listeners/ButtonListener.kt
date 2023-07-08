package michi.bot.listeners

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import michi.bot.commands.mail.inboxMap
import michi.bot.config
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color
import java.util.concurrent.TimeUnit

object ButtonListener: ListenerAdapter() {

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val message = event.message
            val messageEmbeds = message.embeds

            when (event.button.id) {

                "ban-confirmation" -> {
                    val userToBan = event.message.mentions.users[0]
                    val reason = event.message.contentRaw.split("reason: ")[1]

                    event.guild!!.ban(userToBan, 1, TimeUnit.HOURS).reason(reason).queue()

                    val embed = EmbedBuilder().apply {
                        setColor(Color.RED).setTitle("**Ban!** " + Emoji.michiExcite)

                        addField("Banned ${userToBan.name}", "user banned by ${event.user.asMention}", false)

                        if (reason.isNotBlank() && reason.length <= 1750) {
                            addField("Reason: ", reason, false)
                        }
                    }

                    event.replyEmbeds(embed.build()).queue()
                    event.message.delete().queue()
                }

                "clear-mail-inbox-confirmation" -> {
                    if (event.message.components.isNotEmpty()) {
                        event.reply("Successfully cleared your mail inbox. ${Emoji.michiThumbsUp}").setEphemeral(true)
                            .queue()
                        inboxMap[event.user]!!.clear()
                    } else event.reply("You can't use this button anymore.")
                        .setEphemeral(true)
                        .queue()

                    message.editMessageComponents(emptyList()).queue()
                }

                "cancel-mail-inbox-clearing" -> {
                    if (event.message.components.isNotEmpty()) {
                        event.reply("Don't worry, your mails are safe and sound ${Emoji.michiThumbsUp}")
                            .setEphemeral(true)
                            .queue()
                    }
                    else event.reply("You can't use this button anymore.").setEphemeral(true).queue()

                    message.editMessageComponents(emptyList()).queue()
                }

                "report-confirmation" -> {
                    val user = event.user
                    val reportChannel =
                        event.jda.getGuildById(config["BOT_SERVER_ID"])!!.getTextChannelById("1085275324859818025")!!
                    val userInbox = inboxMap[user]!!
                    val mailIndex = messageEmbeds[0].fields[0].value!!.split(" ")[0].removePrefix("#").toInt() - 1
                    event.deferReply(true).queue()

                    if (event.message.components.isEmpty()) return@launch

                    val mailToReport = userInbox[mailIndex]

                    reportChannel.sendMessage("$mailToReport\nsender: ${mailToReport.sender.asMention}\nreceiver: ${user.asMention}\nthis is a manual report.")
                        .queue()

                    event.reply("Thank you reporting ${Emoji.michiThumbsUp}\n We'll try to solve this as quickly as possible\n``Note: We may contact and update you about the report in your DM(Direct Message)``")
                        .setEphemeral(true)
                        .queue()

                    userInbox -= mailToReport
                    message.editMessageComponents(emptyList()).queue()
                }

                "cancel-report" -> {
                    event.deferReply(true)
                    if (event.message.components.isNotEmpty()) event.reply("Report cancelled.").setEphemeral(true)
                        .queue()
                    else event.reply("You can't use this button anymore.").setEphemeral(true).queue()

                    message.editMessageComponents(emptyList()).queue()
                }

                "read-anyway" -> {
                    val user = event.user
                    event.deferReply(true).queue()
                    val mailIndex = message.embeds[0].fields[0].value!!.split(" ")[2].removePrefix("#").toInt() - 1
                    val inbox = inboxMap[user] ?: inboxMap.computeIfAbsent(user) {
                        mutableListOf()
                    }
                    event.reply(inbox[mailIndex].toString())
                        .setEphemeral(true)
                        .queue()
                    message.editMessageComponents(emptyList()).queue()
                }

                "cancel-reading" -> {
                    message.delete().queue()
                }

            }
        }
    }

}