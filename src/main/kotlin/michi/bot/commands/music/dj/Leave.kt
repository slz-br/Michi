package michi.bot.commands.music.dj

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Suppress("Unused")
object Leave: MichiCommand("leave", GUILD_SCOPE) {

    override val userPermissions = listOf(Permission.ADMINISTRATOR)

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return

        val sender = context.user
        val guild = context.guild!!
        val channel = guild.selfMember.voiceState!!.channel!!.asGuildMessageChannel()

        val success: YamlMap = getYML(guild).yamlMap["success_messages"]!!
        val musicDJSuccess: YamlMap = success["music_dj"]!!

        guild.audioManager.closeAudioConnection()

        context.michiReply(String.format(musicDJSuccess.getText("leave_ephemeral_message"), Emoji.michiBlep))
        channel.sendMessage(String.format(musicDJSuccess.getText("leave_public_message"), sender.asMention)).queue()
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val guild = context.guild!!
        val senderVoiceState = sender.voiceState!!
        val bot = guild.selfMember
        val botVoiceState = bot.voiceState!!
        val guildDjMap = GuildDJMap.computeIfAbsent(guild) { mutableSetOf() }

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val musicErr: YamlMap = err["music"]!!

        if (!sender.permissions.any(userPermissions::contains) && sender !in guildDjMap) {
            context.michiReply(String.format(genericErr.getText("user_missing_perms"), Emoji.michiBlep))
            return false
        }

        if (!senderVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        if (!senderVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        if (!botVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        return true
    }
}