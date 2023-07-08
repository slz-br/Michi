package michi.bot.listeners

import kotlinx.coroutines.*
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object CommandAutoCompletionListener: ListenerAdapter() {

    private val timeAutoCompletion = listOf(
        "1 min", "5 min", "10 min", "1 hour", "1 day", "1 week"
    )

    private val slowmodeAutoCompletion = listOf(
        "0", "5s", "10s", "15s", "30s", "1m", "2m", "5m", "10m", "15m", "30m", "1h", "2h", "6h"
    )

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val focusedOption = event.focusedOption
            when (event.name) {

                "mute" -> {
                    if (focusedOption.name == "time") {
                        event.replyChoiceStrings(timeAutoCompletion.filter { it.startsWith(focusedOption.value) })
                            .queue()
                    }
                }

                "slowmode" -> {
                    if (focusedOption.name == "time") {
                        event.replyChoiceStrings(slowmodeAutoCompletion.filter { it.startsWith(focusedOption.value) })
                            .queue()
                    }
                }

            }
        }
    }

}