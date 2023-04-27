package michi.bot.commands

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

abstract class MichiCommand(val name: String, val description: String, val scope: CommandScope) {
    protected open val userPermissions = listOf<Permission>()
    protected open val botPermisions = listOf<Permission>()
    protected open val ownerOnly = false
    protected open val usage = "/$name"
    open val arguments = listOf<MichiArgument>()

    /**
     * Executes the command.
     * @param context The interaction to retrieve info from.
     * @author Slz
     * @see canHandle
     */
    abstract suspend fun execute(context: SlashCommandInteractionEvent)

    /**
     * Checks if it's possible to handle the command.
     * @param context The interaction to check if it's possible to handle.
     * @return True if it's possible to handle, false otherwise.
     * @author Slz
     * @see execute
     */
    protected abstract suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean

}