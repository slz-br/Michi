package michi.bot.commands

import kotlinx.coroutines.*
import michi.bot.util.Emoji
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

private const val DELAY = 5500L

/**
 * Checks if it's possible to perform a slash command, if so, it will be executed.
 * @author Slz
 */
abstract class SlashCommandManager {
    companion object {
        private val cooldownList = mutableSetOf<User>()

        /**
         * Function to put users in cooldown list and then after the delay time passes, remove
         * them from the cooldown list.
         * @param user The user to manage in the cooldown list.
         * @author Slz
         * @see checkUserCooldown
         */
        private suspend fun coolDownManager(user: User) {
            cooldownList.add(user)
            delay(DELAY)
            cooldownList.remove(user)
        }

    }

    /**
     * Function to check if it is possible to perform a command.
     * @param context The slashCommandInteractionEvent to check.
     * @return true if it's possible to execute the command, false otherwise.
     * @author Slz
     * @see execute
     */
    abstract fun check(context: SlashCommandInteractionEvent): Boolean

    /**
     * Function to execute the command.
     * @param context The SlashCommandInteractionEvent to reply to.
     * @author Slz
     * @see check
     * @see checkUserCooldown
     */
    open fun execute(context: SlashCommandInteractionEvent) {
        if (check(context)) return
    }

    /**
     * Checks whether a user is or isn't in cooldown. If the user isn't in cooldown, then this
     * function calls the cooldownManager function.
     * @param context The SlashCommandInteractionEvent to reply to
     * @param user The User to check if it is in cooldown.
     * @return True if the user is in cooldown, false otherwise.
     * @author Slz
     * @see coolDownManager
     */
    protected fun checkUserCooldown(context: SlashCommandInteractionEvent, user: User): Boolean {
        if (cooldownList.contains(user)) {
            context.reply("You're in cooldown, wait a bit ${Emoji.michiSip}").setEphemeral(true).queue()
            return true
        }

        CoroutineScope(Dispatchers.IO).launch {
            coolDownManager(user)
        }
        return false
    }

}