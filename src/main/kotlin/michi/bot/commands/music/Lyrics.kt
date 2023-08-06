package michi.bot.commands.music

import michi.bot.commands.CommandDeactivated
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.lavaplayer.PlayerManager
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@CommandDeactivated
object Lyrics: MichiCommand("Lyrics", GUILD_SCOPE) {

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.VOICE_CONNECT,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        val guild = context.guild!!
        val playingTrack = PlayerManager[guild].player.playingTrack
        TODO("Not yet implemented")
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        TODO("Not yet implemented")
    }
}