package michi.bot.commands.music

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import kotlinx.coroutines.delay
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale

@Suppress("Unused")
object Join: MichiCommand("join", GUILD_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Michi joins the audio channel that you're in",
            DiscordLocale.ENGLISH_UK to "Michi joins the audio channel that you're in",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Michi entra no canal de voz que você está"
        )

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.VOICE_CONNECT,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        val sender = context.member!!
        val guild = context.guild!!

        val audioManager = guild.audioManager
        val channelToJoin = sender.voiceState?.channel ?: return

        audioManager.openAudioConnection(channelToJoin)
        audioManager.isSelfDeafened = true

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val musicSuccess: YamlMap = success["music"]!!

        context.michiReply(String.format(musicSuccess.getText("join"), channelToJoin.asMention, Emoji.michiMusic))

        if (channelToJoin is StageChannel) {
            delay(1500L)
            channelToJoin.requestToSpeak().queue()
        }

    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member ?: return false
        val guild = context.guild ?: return false
        val bot = guild.selfMember
        val senderVoiceState = sender.voiceState ?: return false
        val botVoiceState = bot.voiceState ?: return false

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val musicErr: YamlMap = err["music"]!!

        if (botVoiceState.inAudioChannel()) {
            context.michiReply(musicErr.getText("bot_already_in_vc"))
            return false
        }

        if (!senderVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        val channelToJoin = senderVoiceState.channel ?: return false

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        if (!bot.hasPermission(channelToJoin) || !bot.hasAccess(channelToJoin)) {
            context.michiReply(musicErr.getText("missing_vc_access"))
            return false
        }

        return true
    }

}