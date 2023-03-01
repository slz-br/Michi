package michi.bot.commands

import kotlinx.coroutines.*
import michi.bot.util.Emoji
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

private const val DELAY = 5500L

/**
 * Checks if it is possible to execute a command and manages the users in cooldown.
 * @author Slz
 */
@Deprecated("This class will be replaced by implementations of check function in the commands themselves", level = DeprecationLevel.ERROR)
object CommandManager {
    private val coolDown = mutableListOf<User>()

    /**
     * Puts the user in the cooldown list and remove him from the list after
     * the delay.
     * @param user the user to put in cooldown
     * @author Slz
     */
    private suspend fun coolDownManager(user: User) {
        coolDown.add(user)
        delay(DELAY)
        coolDown.remove(user)
    }

    /**
     * Checks if a user is in cooldown.
     * @param sender The user that sent the command.
     * @param context The slashCommandInteractionEvent that called a function.
     * @return True if the user is in cooldown, false if not.
     * @author Slz
     */
    private fun checkCooldown(sender: User, context: SlashCommandInteractionEvent): Boolean {
        if (coolDown.contains(sender)) {
            context.reply("You are in cooldown, wait a bit ${Emoji.michiSip}")
                .setEphemeral(true)
                .queue()
            return true
        }
        return false
    }

}