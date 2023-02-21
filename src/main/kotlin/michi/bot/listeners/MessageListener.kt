package michi.bot.listeners

import michi.bot.commands.math.MathLogic
import michi.bot.commands.math.checkAnswer
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

        // math
        for (instance in MathProblemManager.instances) {

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