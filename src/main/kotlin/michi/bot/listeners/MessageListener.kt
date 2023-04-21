package michi.bot.listeners

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import michi.bot.commands.math.MathProblemManager
import michi.bot.commands.util.help
import michi.bot.config
import michi.bot.util.Emoji
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.lang.NumberFormatException

private const val DELAY = (1000 * 7).toLong()

/**
 * Called whenever a message is sent.
 * @author Slz
 */
object MessageListener: ListenerAdapter() {

    private val cooldownList = mutableSetOf<User>()
    suspend fun cooldownManager(user: User) {
        cooldownList.add(user)
        delay(DELAY)
        cooldownList.remove(user)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message.contentRaw
        val sender = event.author

        GlobalScope.launch {
            // math
            for (instance in MathProblemManager.instances) {

                if (sender == instance.problemInstance.user && !sender.isBot && event.channel == instance.context.channel) {
                    try {
                        msg.toInt()

                        instance.checkAnswer(event, instance)
                    } catch (e: NumberFormatException) {
                        return@launch
                    }
                }

            }

            // help
            when (msg) {
                "<@${config["BOT_ID"]}>" -> {
                    if (!userIsInCooldowm(sender)) help(event)
                    else event.message.reply("You are in cooldown, wait a bit ${Emoji.michiSip}").queue()
                    cooldownManager(sender)
                }
            }

        }

    }

    private fun userIsInCooldowm(user: User): Boolean {
        if (cooldownList.contains(user)) return true
        return false
    }

}