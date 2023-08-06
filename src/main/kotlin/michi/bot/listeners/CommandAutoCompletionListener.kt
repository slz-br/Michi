package michi.bot.listeners

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import michi.bot.util.Language
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Class that holds the event handler [onCommandAutoCompleteInteraction].
 * @author Slz
 */
object CommandAutoCompletionListener: ListenerAdapter() {

    val timeAutoCompletion = listOf(
        "1 min", "5 min", "10 min", "1 hour", "1 day", "1 week"
    )

    val slowmodeAutoCompletion = listOf(
        "0", "5s", "10s", "15s", "30s", "1m", "2m", "5m", "10m", "15m", "30m", "1h", "2h", "6h"
    )

    val pollAutoCompletion = listOf(
        "30s", "1m", "5m", "10m", "15m", "30m", "1h", "2h", "6h", "1d", "7d"
    )

    val languageAutoCompletion = listOf(
        Language.EN_US.value, Language.PT_BR.value
    )

    /**
     * Called whenever an argument with auto-completion is accessed.
     * @author Slz
     */
    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        CoroutineScope(IO).launch {
            val focusedOption = event.focusedOption
            when (event.name) {

                "mute" -> {
                    if (focusedOption.name != "time") return@launch
                    event.replyChoiceStrings(timeAutoCompletion.filter { it.startsWith(focusedOption.value) })
                        .queue()
                }

                "slowmode" -> {
                    if (focusedOption.name != "time") return@launch
                    event.replyChoiceStrings(slowmodeAutoCompletion.filter { it.startsWith(focusedOption.value) })
                        .queue()
                }

                "poll" -> {
                    if (focusedOption.name != "time") return@launch
                    event.replyChoiceStrings(pollAutoCompletion.filter { it.startsWith(focusedOption.value) })
                        .queue()
                }

                "language" -> {
                    if (focusedOption.name != "language-name") return@launch
                    event.replyChoiceStrings(languageAutoCompletion)
                        .queue()
                }

            }
        }
    }

}