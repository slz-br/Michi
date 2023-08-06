package michi.bot.commands.music.dj

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.database.dao.GuildsDAO
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Suppress("Unused")
object ForceSkip: MichiCommand("fskip", GUILD_SCOPE) {

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
        val channel = guild.selfMember.voiceState!!.channel!!.asGuildMessageChannel()
        val musicManager = PlayerManager[guild]
        val sender = context.user

        musicManager.scheduler.nextTrack()

        val playingTrack = musicManager.playingTrack

        playingTrack?.let {
            GuildsDAO.getMusicQueue(guild)?.replace(playingTrack.info.uri, "")?.let {
                GuildsDAO.setMusicQueue(guild, it)
            }
        }

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val musicDjSuccess: YamlMap = success["music_dj"]!!

        context.michiReply(String.format(musicDjSuccess.getText("force_skip_ephemeral_message"), Emoji.michiThumbsUp))
        channel.sendMessage(String.format(musicDjSuccess.getText("force_skip_public_message"), sender.asMention))
            .queue()
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild!!
        val sender = context.member!!
        val bot = guild.selfMember
        val botVoiceState = bot.voiceState!!
        val senderVoiceState = sender.voiceState!!
        val guildDjMap = GuildDJMap.computeIfAbsent(guild) { mutableSetOf() }

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val musicErr: YamlMap = err["music"]!!
        
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
            audioManager.isSelfDeafened = true
        }

        if (!botVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        return true
    }

}