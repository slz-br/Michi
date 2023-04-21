package michi.bot.listeners

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object CommandAutoCompletionListener: ListenerAdapter() {

    private val muteAutoCompletionOptions = listOf(
        "1 min", "5 min", "10 min", "1 hour", "1 day", "1 week"
    )

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        val focusedOption = event.focusedOption

        when (event.name) {

            "mute" -> {
                if (focusedOption.name == "time") {
                    event.replyChoiceStrings(muteAutoCompletionOptions.filter { it.startsWith(focusedOption.value) }).queue()
                }
            }

        }
    }

}