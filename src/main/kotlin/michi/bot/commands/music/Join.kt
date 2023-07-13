package michi.bot.commands.music

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiCommand
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Suppress("Unused")
object Join: MichiCommand("join", "Michi joins the audio channel that you're in.", CommandScope.GUILD_SCOPE) {

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.VOICE_CONNECT,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/join"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.member ?: return
        val senderAsUser = context.user
        val guild = context.guild ?: return

        if (!canHandle(context)) return

        val audioManager = guild.audioManager
        val channelToJoin = sender.voiceState?.channel ?: return

        audioManager.openAudioConnection(channelToJoin)

        context.reply("Joined ${channelToJoin.asMention}\nPut some music on ${Emoji.michiMusic}")
            .queue()

        // puts the user that sent the command in cooldown
        SlashCommandListener.cooldownManager(senderAsUser)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member ?: return false
        val guild = context.guild ?: return false
        val bot = guild.selfMember
        val senderVoiceState = sender.voiceState ?: return false
        val botVoiceState = bot.voiceState ?: return false

        if (botVoiceState.inAudioChannel()) {
            context.reply("I'm already in a voice channel.")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!senderVoiceState.inAudioChannel()) {
            context.reply("You need to be in a voice channel to use this command.")
                .setEphemeral(true)
                .queue()
            return false
        }

        val channelToJoin = senderVoiceState.channel ?: return false

        if (!bot.permissions.containsAll(botPermissions)) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (!bot.hasPermission(channelToJoin) || !bot.hasAccess(channelToJoin)) {
            context.reply("I don't have permission to join this voice channel ${Emoji.michiSad}")
                .setEphemeral(true)
                .queue()
            return false
        }

        return true
    }

}