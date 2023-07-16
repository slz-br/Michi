package michi.bot.commands.util

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandDeactivated
object UserInfo: MichiCommand("user-info", "Sends you info about the user you chose", CommandScope.GLOBAL_SCOPE) {

    override val usage: String
        get() = "/user-info <user>"

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("user", "The user to get info", OptionType.USER)
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        TODO("Not yet implemented")
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        TODO("Not yet implemented")
    }
}