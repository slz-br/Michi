package michi.bot.commands.music.dj

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiCommand
import michi.bot.lavaplayer.PlayerManager
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Suppress("Unused")
object Shuffle: MichiCommand("queue-shuffle", "Shuffles the queue.", CommandScope.GUILD_SCOPE) {

    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR
        )

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/shuffle"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user

        if (!canHandle(context)) return

        val guild = context.guild ?: return
        val musicManager = PlayerManager.getMusicManager(guild)
        val queue = musicManager.scheduler.trackQueue

        queue.shuffled().toCollection(queue)
        context.reply("queue sucessfully shuffled ${Emoji.michiThumbsUp}")
            .setEphemeral(true)
            .queue()

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild ?: return false
        val sender = context.member ?: return false
        val bot = guild.selfMember
        val musicManager = PlayerManager.getMusicManager(guild)
        val queue = musicManager.scheduler.trackQueue

        if (queue.isEmpty()) {
            context.reply("The queue is empty ${Emoji.michiSad}").setEphemeral(true).queue()
            return false
        }

        if (!sender.permissions.any { permission -> userPermissions.contains(permission) }) {
            context.reply("You don't have the permissions to use this command, silly you ${Emoji.michiBlep}").setEphemeral(true).queue()
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}").setEphemeral(true).queue()
            return false
        }

        return true
    }
}