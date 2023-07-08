package michi.bot.commands.music.dj

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Suppress("Unused")
object Leave: MichiCommand("leave", "Disconnects michi from the call", CommandScope.GUILD_SCOPE) {
    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val channel = context.channel
        val guild = context.guild ?: return

        if (!canHandle(context)) return

        guild.audioManager.closeAudioConnection()

        context.reply("Bye! ${Emoji.michiBlep}")
            .setEphemeral(true)
            .queue()

        channel.sendMessage("${sender.asMention} Ordered me to leave the voice channel.")
            .queue()

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member ?: return false
        val senderVoiceState = sender.voiceState ?: return false
        val bot = context.guild?.selfMember ?: return false
        val botVoiceState = bot.voiceState ?: return false

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
            context.reply("You need to be in a voice channel to use this command")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!botVoiceState.inAudioChannel()) {
            context.reply("I ain't in a voice channel.")
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

        return true
    }
}