package michi.bot.commands

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
abstract class MichiCommand(val name: String, val description: String, val scope: CommandScope) {
    open val userPermissions = listOf<Permission>()
    open val botPermisions = listOf<Permission>()
    open val ownerOnly = false
    open val usage = ""
    open val arguments = listOf<MichiArgument>()

    /**
     * Executes the command.
     * @param context The interaction to retrieve info from.
     * @author Slz
     * @see canHandle
     */
    abstract fun execute(context: SlashCommandInteractionEvent)

    /**
     * Checks if it's possible to handle the command.
     * @param context The interaction to check if it's possible to handle.
     * @return True if it's possible to handle, false otherwise.
     * @author Slz
     * @see execute
     */
    abstract fun canHandle(context: SlashCommandInteractionEvent): Boolean

}