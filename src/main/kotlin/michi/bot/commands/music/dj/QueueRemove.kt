package michi.bot.commands.music.dj

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.database.dao.GuildsDAO
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.concurrent.TimeUnit

@Suppress("Unused")
object QueueRemove: MichiCommand("queue-remove", GUILD_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Removes a music at a specific position from the queue",
            DiscordLocale.ENGLISH_UK to "Removes a music at a specific position from the queue",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Remove uma música"
        )

    override val userPermissions = listOf(Permission.ADMINISTRATOR)

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.VOICE_CONNECT,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val arguments = listOf(
        MichiArgument(
            name = "position",
            descriptionLocalization = mapOf(
                DiscordLocale.ENGLISH_US to "The position of the track to remove in the queue",
                DiscordLocale.ENGLISH_UK to "The position of the track to remove in the queue",
                DiscordLocale.PORTUGUESE_BRAZILIAN to "A posição na fila da música para remover."
            ),
            type = OptionType.INTEGER
        )
    )

    override val usage: String
        get() = "/$name <position>"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        val sender = context.user
        val guild = context.guild!!
        val musicManager = PlayerManager[guild]
        val position = context.getOption("position")!!.asInt - 1
        val queue = musicManager.scheduler.trackQueue

        val channel = guild.selfMember.voiceState!!.channel?.asGuildMessageChannel()
        val trackToRemove = queue.elementAt(position)
        queue -= trackToRemove

        val guildMusicQueue = GuildsDAO.getMusicQueue(guild)
        guildMusicQueue?.replace(trackToRemove.info.uri, "")?.let {
            GuildsDAO.setMusicQueue(guild, it)
        }

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val musicDjSuccess: YamlMap = success["music_dj"]!!
        val successEphemeral: YamlMap = getYML(sender).yamlMap["success_messages"]!!
        val musicDjSuccessEphemeral: YamlMap = successEphemeral["music_dj"]!!

        context.michiReply(String.format(musicDjSuccessEphemeral.getText("queue_remove_ephemeral_message"), trackToRemove.info.title, formatTrackLength(trackToRemove), position + 1))
        channel?.sendMessage(String.format(musicDjSuccess.getText("queue_remove_public_message"), sender.asMention, trackToRemove.info.title, formatTrackLength(trackToRemove)))?.queue()
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild!!
        val sender = context.member!!
        val bot = guild.selfMember
        val senderVoiceState = sender.voiceState!!
        val botVoiceState = bot.voiceState!!
        val musicManager = PlayerManager[guild]
        val queue = musicManager.scheduler.trackQueue
        val position = context.getOption("position")!!.asInt - 1
        val guildDjMap = GuildDJMap.computeIfAbsent(guild) { mutableSetOf() }

        val err: YamlMap = getYML(sender.user).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val musicErr: YamlMap = err["music"]!!

        if (queue.isEmpty()) {
            context.michiReply(String.format(musicErr.getText("empty_queue"), Emoji.michiSad))
            return false
        }

        if (position < 0 || position > queue.size - 1) {
            context.michiReply(genericErr.getText("invalid_position"))
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        if (!sender.permissions.any(userPermissions::contains) && sender !in guildDjMap) {
            context.michiReply(String.format(genericErr.getText("user_missing_perms"), Emoji.michiBlep))
            return false
        }

        if (!senderVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        if (!botVoiceState.inAudioChannel() && bot.hasAccess(senderVoiceState.channel!!)) {
            val audioManager = guild.audioManager
            val channelToJoin = senderVoiceState.channel

            audioManager.openAudioConnection(channelToJoin)
        }

        if (!botVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        if (senderVoiceState.channel != botVoiceState.channel) {
            context.michiReply(musicErr.getText("user_not_in_bot_vc"))
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