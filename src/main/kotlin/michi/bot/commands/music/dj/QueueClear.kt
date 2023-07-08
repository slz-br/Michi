package michi.bot.commands.music.dj

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiCommand
import michi.bot.database.dao.GuildsDAO
import michi.bot.lavaplayer.PlayerManager
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Suppress("Unused")
object QueueClear: MichiCommand("queue-clear", "Clears the entire music queue", CommandScope.GUILD_SCOPE) {

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
        get() = "/queue-clear"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val guild = context.guild!!
        val sender = context.user
        val channel = context.channel
        val musicManager = PlayerManager.getMusicManager(guild)

        if (!canHandle(context)) return

        musicManager.scheduler.trackQueue.clear()

        context.reply("The queue was successfully cleared ${Emoji.michiThumbsUp}").setEphemeral(true).queue()
        channel.sendMessage("${sender.asMention} cleared the queue").queue()

        GuildsDAO.setMusicQueue(guild, queue = null)

        /* puts the user that sent the command in cooldown */
        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild ?: return false
        val sender = context.member ?: return false
        val bot = guild.selfMember
        val senderVoiceState = sender.voiceState ?: return false
        val botVoiceState = bot.voiceState ?: return false
        val queue = PlayerManager.getMusicManager(guild).scheduler.trackQueue

        if (queue.isEmpty()) {
            context.reply("The queue is empty").setEphemeral(true).queue()
        }

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

        return true
    }
}