package michi.bot.commands

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

/**
 * Abstract class used to create slash commands.
 * @param name The name of the command.
 * @param description A brief description of the command.
 * @param scope The scope of the command. More about CommandScope can be found at [CommandScope]
 * @author Slz
 */
@Suppress("Unused")
abstract class MichiCommand(val name: String, val description: String, val scope: CommandScope) {

    /**
     * A list of permissions that the user need to execute the command.
     * Default = empty list.
     * @see botPermissions
     */
    protected open val userPermissions = listOf<Permission>()

    /**
     * A List of permissions for Michi so she can run the commands.
     * Default = List with MESSAGE_SEND, MESSAGE_EXT_EMOJI and MESSAGE_SEND_IN_THREADS
     * permissions
     * @see userPermissions
     */
    protected open val botPermissions = listOf(
        Permission.MESSAGE_SEND,
        Permission.MESSAGE_EXT_EMOJI,
        Permission.MESSAGE_SEND_IN_THREADS
    )

    /**
     * True if only the owner can execute the command, false otherwise.
     * Default = false.
     */
    protected open val ownerOnly = false

    /**
     * String to show how to use the command.
     * Default = "/$commandName".
     */
    protected open val usage = "/$name"

    /**
     * List of arguments for the command.
     * Example = /mute (time)
     * In this example the "(time)" represents an argument.
     * Default = empty list.
     * @see MichiArgument
     */
    open val arguments = listOf<MichiArgument>()

    /**
     * Tries to execute the command.
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