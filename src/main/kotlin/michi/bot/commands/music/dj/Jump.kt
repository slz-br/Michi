package michi.bot.commands.music.dj

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.lavaplayer.PlayerManager
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

@Suppress("Unused")
object Jump: MichiCommand("queue-jump", "Jumps to a specific song", CommandScope.GUILD_SCOPE) {

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument(
                "position",
                "The position in the queue to jump to",
                OptionType.INTEGER,
            )
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val guild = context.guild ?: return
        val position = context.getOption("position")?.asInt ?: return
        val scheduler = PlayerManager.getMusicManager(guild).scheduler

        if (!canHandle(context)) return

        scheduler.playTrackAt(position)

        context.reply("${sender.asMention} jumped to the track at position ${position - 1} in the queue.\n#${position - 1} ${scheduler.trackQueue.elementAt(position - 1).info.title}")
            .queue()

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member ?: return false
        val guild = context.guild ?: return false
        val bot = guild.selfMember
        val position = context.getOption("position")?.asInt ?: return false
        val queue = PlayerManager.getMusicManager(guild).scheduler.trackQueue

        if (sender.permissions.any { permission -> userPermissions.contains(permission) }) {
            context.reply("You don't have permission to use this command, silly you ${Emoji.michiBlep}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (position > queue.size - 1) {
            context.reply("Invalid position")
                .setEphemeral(true)
                .queue()
            return false
        }

        return true
    }

}