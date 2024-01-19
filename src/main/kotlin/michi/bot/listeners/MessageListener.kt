package michi.bot.listeners

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import michi.bot.commands.misc.Math
import michi.bot.commands.misc.TypeRacer
import michi.bot.config
import michi.bot.database.dao.TypeRacerDao
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.lang.NumberFormatException
import java.util.concurrent.TimeUnit

/**
 * Class that holds the event handler [onMessageReceived].
 * @author Slz
 */
object MessageListener: ListenerAdapter() {
    private const val DELAY = (1000 * 7).toLong()

    private val cooldownList = mutableSetOf<User>()

    private suspend fun cooldownManager(user: User) {
        cooldownList += user
        delay(DELAY)
        cooldownList -= user
    }

    private val mutex = Mutex()

    /**
     * Called whenever a message is sent.
     * @author Slz
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message.contentRaw
        val sender = event.author

        GlobalScope.launch {

            val err: YamlMap = getYML(event).yamlMap["error_messages"]!!
            val genericErr: YamlMap = err["generic"]!!
            val warn: YamlMap = getYML(event).yamlMap["warn_messages"]!!
            val genericWarn: YamlMap = warn["generic"]!!

            // math
            Math.MathProblemManager.instances.forEach { instance ->
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
                if (event.author in cooldownList) {
                    event.message.reply(String.format(genericErr.getText("user_in_command_cooldown"), Emoji.michiSip))
                        .queue()
                }
                event.message.reply(genericWarn.getText("use_help_command"))
                    .queue()
            }

            if (msg.startsWith("tr ")) {
                val userSession = TypeRacer.sessions[sender] ?: return@launch
                val timeInMiliss = (System.currentTimeMillis() - userSession.startTime)
                val wordsToType = userSession.text.split(" ")

                var counter = 0.toFloat()
                msg.removePrefix("tr ").split(" ").forEachIndexed { index, text ->
                   if (text.equals(wordsToType[index], ignoreCase = true)) counter++
                }

                userSession.isAnswered = true

                val timeFormated = "${(timeInMiliss / TimeUnit.SECONDS.toMillis(1))}s ${timeInMiliss % TimeUnit.SECONDS.toMillis(1)}ms"
                val averageWordsPerMinute = String.format("%.3f", ((counter / timeInMiliss) * 60000))

                event.message.reply("CW: $counter | $timeFormated | WPM: $averageWordsPerMinute")
                    .queue()

                val wordsPerMinutesAsFloat = (counter / timeInMiliss) * 60000

                TypeRacerDao.putLatest(sender, wordsPerMinutesAsFloat)

                if (wordsPerMinutesAsFloat > TypeRacerDao.getScoresMap(sender)["pb"]!!) {
                    TypeRacerDao.putPersonalBest(sender, wordsPerMinutesAsFloat)
                }
            }
            mutex.withLock { cooldownManager(event.author) }
        }

    }

}