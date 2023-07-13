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
object ForceSkip: MichiCommand("fskip", "Force a track to be skipped", CommandScope.GUILD_SCOPE) {

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
        get() = "/fskip"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val guild = context.guild!!
        val musicManager = PlayerManager.getMusicManager(guild)
        val sender = context.user

        if (!canHandle(context)) return

        musicManager.scheduler.nextTrack()

        context.reply("Successfully skipped ${Emoji.michiThumbsUp}").setEphemeral(true).queue()
        context.channel.sendMessage("Current music force skipped by ${sender.asMention}").queue()

        // puts the user that sent the command in cooldown
        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild!!
        val sender = context.member!!
        val bot = guild.selfMember
        val botVoiceState = bot.voiceState!!
        val channel = context.channel
        val senderVoiceState = sender.voiceState!!

        if (!bot.permissions.containsAll(botPermissions)) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!sender.permissions.any { permission -> userPermissions.contains(permission) }) {
            context.reply("You don't have permission to use this command, silly you ${Emoji.michiBlep}")
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

        if (!botVoiceState.inAudioChannel()) {
            val audioManager = guild.audioManager
            val channelToJoin = senderVoiceState.channel

            if (channelToJoin == null) {
                context.reply("Something went really wrong ${Emoji.michiOpsie}")
                    .setEphemeral(true)
                    .queue()
                return false
            }

            audioManager.openAudioConnection(channelToJoin)
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

        return true
    }

}