package michi.bot.listeners

import michi.bot.commands.math.MathLogic
import michi.bot.commands.math.checkAnswer
import michi.bot.commands.walterwhite.sendRandomWalterWhiteImage
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.lang.NumberFormatException

/**
 * Called whenever there's a MessageReceivedEvent
 * @author Slz
 */
class MessageListener: ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message.contentRaw
        val sender = event.author

        // walter white
        if (msg.contains("walter") || msg.contains("white")) {
            sendRandomWalterWhiteImage(event)
        }

        // math
        // currently it throws concurrentModificationException
        for (instance in MathLogic.instances) {

            if (sender == instance.problemInstance.user && !sender.isBot && event.channel == instance.context.channel) {
                try {
                    msg.toInt()
                    checkAnswer(event, instance)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }

        }

    }

}