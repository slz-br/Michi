package michi.bot.commands.music.dj

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiCommand
import michi.bot.lavaplayer.PlayerManager
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Suppress("Unused")
object ForcePause: MichiCommand("fpause", "Forces the current track to be paused.", CommandScope.GUILD_SCOPE) {
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
        get() = "/fpause"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val guild = context.guild
        val channel = context.channel
        if (!canHandle(context)) return

        val musicManager = PlayerManager.getMusicManager(guild!!)
        val player = musicManager.player
        player.isPaused = true

        context.reply("Successfully paused the track ${Emoji.michiThumbsUp}").setEphemeral(true).queue()
        channel.sendMessage("${sender.asMention} force paused the current track").queue()

        /* puts the user that sent the command in cooldown */
        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild ?: return false
        val sender = context.member ?: return false
        val bot = guild.selfMember
        val senderVoiceState = sender.voiceState ?: return false
        val botVoiceState = bot.voiceState ?: return false
        val channel = context.channel
        val player = PlayerManager.getMusicManager(guild).player

        if (!sender.permissions.any { permission -> userPermissions.contains(permission) }) {
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

        if (!senderVoiceState.inAudioChannel()) {
            context.reply("You need to be in a voice channel to use this command ${Emoji.michiBlep}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (senderVoiceState.channel != botVoiceState.channel) {
            context.reply("You need to be in the same voice channel as me to use this command")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (channel is TextChannel && !bot.hasPermission(channel)) {
            context.reply("I don't have permission to message in this channel")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (player.isPaused) {
            context.reply("The queue is already paused")
                .setEphemeral(true)
                .queue()
            return false
        }

        return true
    }
}