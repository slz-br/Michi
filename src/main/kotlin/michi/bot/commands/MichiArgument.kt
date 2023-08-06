package michi.bot.commands

import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * Used to define arguments of a MichiCommand.
 *
 * [name] The name of the argument.
 *
 * [type] The type of the argument(e.g: [OptionType.STRING] for strings)
 *
 * [isRequired] Determines if the argument is obligatory or not. Default = true
 *
 * [hasAutoCompletion] Determines if the argument has autocompletion or
 * not(note that some OptionTypes don't support autocompletion). Default = false
 *
 * @author Slz
 */
data class MichiArgument(
    val name: String,
    val type: OptionType,
    val isRequired: Boolean = true,
    val hasAutoCompletion: Boolean = false
)