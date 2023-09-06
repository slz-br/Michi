package michi.bot.commands.music.dj

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale

@Suppress("Unused")
object Shuffle: MichiCommand("queue-shuffle", GUILD_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Shuffles the queue",
            DiscordLocale.ENGLISH_UK to "Shuffles the queue",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Bagunça a fila(randomiza a ordem da fila)"
        )

    override val userPermissions = listOf(Permission.ADMINISTRATOR)

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.VOICE_CONNECT,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return

        val sender = context.user
        val guild = context.guild!!
        val musicManager = PlayerManager[guild]
        val queue = musicManager.scheduler.trackQueue
        val channel = guild.selfMember.voiceState!!.channel?.asGuildMessageChannel()

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val musicDjSuccess: YamlMap = success["music_dj"]!!

        val newQueue = queue.shuffled()
        queue.clear()
        newQueue.toCollection(queue)
        context.michiReply(String.format(musicDjSuccess.getText("queue_shuffle_ephemeral_message"), Emoji.michiThumbsUp))
        channel?.sendMessage(String.format(musicDjSuccess.getText("queue_shuffle_public_message"), sender.asMention))?.queue()
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild ?: return false
        val sender = context.member ?: return false
        val bot = guild.selfMember
        val musicManager = PlayerManager[guild]
        val queue = musicManager.scheduler.trackQueue
        val senderVoiceState = sender.voiceState!!
        val botVoiceState = bot.voiceState!!
        val guildDjMap = GuildDJMap.computeIfAbsent(guild) { mutableSetOf() }

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val musicErr: YamlMap = err["music"]!!

        if (queue.isEmpty()) {
            context.michiReply(String.format(musicErr.getText("empty_queue"), Emoji.michiSad))
            return false
        }

        if (!sender.permissions.any(userPermissions::contains) && sender !in guildDjMap) {
            context.michiReply(String.format(genericErr.getText("user_missing_perms"), Emoji.michiBlep))
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
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

        return true
    }
}