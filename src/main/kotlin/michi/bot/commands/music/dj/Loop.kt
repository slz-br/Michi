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
object Loop: MichiCommand("loop", GUILD_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Play a track repeatedly until it's disabled",
            DiscordLocale.ENGLISH_UK to "Play a track repeatedly until it's disabled",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Toca a música repetidamente até que desligado"
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
        val guild = context.guild!!
        val scheduler = PlayerManager[guild].scheduler
        val channel = guild.selfMember.voiceState!!.channel?.asGuildMessageChannel()
        val sender = context.user

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val musicDjSuccess: YamlMap = success["music_dj"]!!
        val successEphemeral: YamlMap = getYML(sender).yamlMap["success_messages"]!!
        val musicDjSuccessEphemeral: YamlMap = successEphemeral["music_dj"]!!

        scheduler.isLooping = !scheduler.isLooping

        if (scheduler.isLooping) {
            context.michiReply(String.format(musicDjSuccessEphemeral.getText("loop_enabled_ephemeral_message"), Emoji.michiThumbsUp)) // "now Looping {s}", current playing track
            channel?.sendMessage(String.format(musicDjSuccess.getText("loop_enabled_public_message"), sender.asMention))
                ?.queue()
            return
        }

        context.michiReply(musicDjSuccess.getText("loop_disabled_ephemeral_message")) // "loop disabled"
        channel?.sendMessage(String.format(musicDjSuccess.getText("loop_disabled_public_message"), sender.asMention))?.queue()
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild!!
        val sender = context.member!!
        val bot = guild.selfMember
        val botVoiceState = bot.voiceState!!
        val senderVoiceState = sender.voiceState!!
        val player = PlayerManager[guild].player
        val guildDjMap = GuildDJMap.computeIfAbsent(guild) { mutableSetOf() }

        val err: YamlMap = getYML(sender.user).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val musicDJErr: YamlMap = err["music_dj"]!!

        if (player.playingTrack == null) {
            context.michiReply(musicDJErr.getText("loop_nothing_playing"))
            return false
        }

        if (!sender.permissions.any(userPermissions::contains) && sender !in guildDjMap) {
            context.michiReply(String.format(genericErr.getText("user_missing_perms"), Emoji.michiBlep))
            return false
        }

        if (!senderVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicDJErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        if (!botVoiceState.inAudioChannel() && bot.hasAccess(senderVoiceState.channel!!)) {
            val audioManager = guild.audioManager
            val channelToJoin = senderVoiceState.channel

            audioManager.openAudioConnection(channelToJoin)
            audioManager.isSelfDeafened = true
        }

        if (!botVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicDJErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        return true
    }
}