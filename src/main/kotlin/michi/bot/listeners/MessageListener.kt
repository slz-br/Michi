package michi.bot.listeners

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import michi.bot.commands.misc.MathProblemManager
import michi.bot.config
import michi.bot.util.Emoji
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.lang.NumberFormatException

/**
 * Called whenever a message is sent.
 * @author Slz
 */
object MessageListener: ListenerAdapter() {
    private const val DELAY = (1000 * 7).toLong()

    private val cooldownList = mutableSetOf<User>()

    suspend fun cooldownManager(user: User) {
        cooldownList += user
        delay(DELAY)
        cooldownList -= user
    }

    /**
     * @throws NumberFormatException
     * @author Slz
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message.contentRaw
        val sender = event.author

        GlobalScope.launch {

            // math
            MathProblemManager.instances.forEach { instance ->
                if (sender == instance.problemInstance.user && event.channel == instance.context.channel) {
                    try {
                        msg.toInt()
                        instance.checkAnswer(event, instance)
                    } catch (e: NumberFormatException) {
                        return@launch
                    }
                }
            }

            // help
            if (msg == "<@${config["BOT_ID"]}>") {
                if (cooldownList.contains(event.author)) {
                    event.message.reply("You're in cooldown, wait a bit ${Emoji.michiSip}")
                        .queue()
                }
                event.message.reply("Use /help for help")
                    .queue()
            }

        }

    }

}