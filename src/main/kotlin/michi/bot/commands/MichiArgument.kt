package michi.bot.commands

import net.dv8tion.jda.api.interactions.commands.OptionType

data class MichiArgument(
    val name: String,
    val description: String,
    val type: OptionType,
    val isRequired: Boolean,
    val hasAutoCompletion: Boolean
)