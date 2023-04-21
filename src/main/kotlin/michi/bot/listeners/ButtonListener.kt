package michi.bot.listeners

import michi.bot.commands.mail.Mail
import michi.bot.config
import michi.bot.util.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object ButtonListener: ListenerAdapter() {

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val message = event.message
        val messageEmbeds = message.embeds

        when(event.button.id) {

            "clear-mail-inbox-confirmation" -> {
                if (event.message.components.isNotEmpty()) {
                    event.reply("Successfully cleared your mail inbox. ${Emoji.michiThumbsUp}").setEphemeral(true).queue()
                    Mail.inboxMap[event.user]!!.clear()
                }
                else event.reply("You can't use this button anymore.").setEphemeral(true).queue()

                event.message.editMessage(event.message.contentRaw).setComponents().queue()
            }

            "cancel-mail-inbox-clearing" -> {
                if (event.message.components.isNotEmpty()) event.reply("Don't worry, your mails are safe and sound ${Emoji.michiThumbsUp}").setEphemeral(true).queue()
                else event.reply("You can't use this button anymore.").setEphemeral(true).queue()

                event.message.editMessage(event.message.contentRaw).setComponents().queue()
            }

            "report-confirmation" -> {
                val user = event.user
                val reportChannel = event.jda.getGuildById(config["BOT_SERVER_ID"])!!.getTextChannelById("1085275324859818025")!!
                val userInbox = Mail.inboxMap[user]!!
                val mailIndex = messageEmbeds[0].fields[0].value!![1].digitToInt() - 1

                if (event.message.components.isNotEmpty()) {
                    val mailToReport = userInbox[mailIndex]

                    reportChannel.sendMessage("$mailToReport\nsender: ${mailToReport.sender.asMention}\nreceiver: ${user.asMention}\nthis is a manual report.")
                        .queue()

                    event.reply("Thank you reporting ${Emoji.michiThumbsUp}\n We'll try to solve this as quickly as possible\n``Note: We may contact and update you about the report in your DM(Direct Message)``")
                        .setEphemeral(true)
                        .queue()

                    userInbox.remove(mailToReport)
                }
                else event.reply("You can't use this button anymore.").setEphemeral(true).queue()

                event.message.editMessageEmbeds(event.message.embeds[0]).setComponents().queue()
            }

            "cancel-report" -> {
                if (event.message.components.isNotEmpty()) event.reply("Report cancelled.").setEphemeral(true).queue()
                else event.reply("You can't use this button anymore.").setEphemeral(true).queue()

                event.message.editMessageEmbeds(event.message.embeds[0]).setComponents().queue()
            }

        }

    }

}