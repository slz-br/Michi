package michi.bot.commands.music.dj

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import michi.bot.commands.CommandScope
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.lavaplayer.PlayerManager
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.concurrent.TimeUnit

@Suppress("Unused")
object QueueRemove: MichiCommand("queue-remove", "Removes a music at a specific position from the queue", CommandScope.GUILD_SCOPE) {

    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR
        )

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_SEND_IN_THREADS
        )
    override val usage: String
        get() = "/queue-remove <position>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("position", "The position of the track to remove.", OptionType.INTEGER)
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val guild = context.guild!!
        val musicManager = PlayerManager.getMusicManager(guild)
        val sender = context.user
        val position = context.getOption("position")!!.asInt - 1
        val queue = musicManager.scheduler.trackQueue

        if (!canHandle(context)) return

        val trackToRemove = queue.elementAt(position)
        queue -= trackToRemove

        context.reply("Successfully removed ${trackToRemove.info.title}`[${formatTrackLength(trackToRemove)}]` at the position ${position + 1} from the queue")
            .queue()

        // puts the user that sent the command in cooldown
        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild!!
        val sender = context.member!!
        val bot = guild.selfMember
        val senderVoiceState = sender.voiceState!!
        val botVoiceState = bot.voiceState!!
        val musicManager = PlayerManager.getMusicManager(guild)
        val queue = musicManager.scheduler.trackQueue
        val position = context.getOption("position")!!.asInt - 1

        if (queue.isEmpty()) {
            context.reply("The queue is empty ${Emoji.michiSad}")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (position < 0 || position > queue.size - 1) {
            context.reply("Invalid position")
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

        if (senderVoiceState.channel?.asVoiceChannel() != botVoiceState.channel?.asVoiceChannel()) {
            context.reply("You need to be in the same voice channel as me to use this command")
                .setEphemeral(true)
                .queue()
            return false
        }

        return true
    }

    private fun formatTrackLength(track: AudioTrack): String {
        val timeInMiliss = track.duration

        val hours = timeInMiliss / TimeUnit.HOURS.toMillis(1)
        val minutes = timeInMiliss / TimeUnit.MINUTES.toMillis(1)
        val seconds = timeInMiliss % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1)

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

}