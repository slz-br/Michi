package michi.bot.commands.music.dj

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

@Suppress("Unused")
object Jump: MichiCommand("queue-jump", GUILD_SCOPE) {

    override val arguments = listOf(MichiArgument("position", OptionType.INTEGER))

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.VOICE_CONNECT,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/$name <position>"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val guild = context.guild ?: return
        val position = context.getOption("position")?.asInt ?: return
        val scheduler = PlayerManager[guild].scheduler
        val channel = guild.selfMember.voiceState!!.channel?.asGuildMessageChannel()

        if (!canHandle(context)) return

        scheduler.playTrackAt(position)

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val musicDjSuccess: YamlMap = success["music_dj"]!!

        context.michiReply(String.format(musicDjSuccess.getText("queue_jump_ephemeral_message"), position - 1, position - 1, scheduler.trackQueue.elementAt(position - 1).info.title))
        channel?.sendMessage(String.format(musicDjSuccess.getText("queue_jump_public_message"), sender.asMention, position - 1, position - 1, scheduler.trackQueue.elementAt(position - 1).info.title))
            ?.queue()
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val guild = context.guild!!
        val bot = guild.selfMember
        val position = context.getOption("position")?.asInt ?: return false
        val queue = PlayerManager[guild].scheduler.trackQueue
        val channel = bot.voiceState!!.channel?.asGuildMessageChannel()
        val senderVoiceState = sender.voiceState!!
        val botVoiceState = bot.voiceState!!
        val guildDjMap = GuildDJMap.computeIfAbsent(guild) { mutableSetOf() }

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val musicErr: YamlMap = err["music"]!!
        
        if (sender.permissions.any(userPermissions::contains) && sender !in guildDjMap) {
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
            audioManager.isSelfDeafened = true
        }

        if (!botVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        if (senderVoiceState.channel != channel) {
            context.michiReply(musicErr.getText("user_not_in_bot_vc"))
            return false
        }

        if (position > queue.size - 1) {
            context.michiReply(genericErr.getText("invalid_position"))
            return false
        }

        return true
    }

}