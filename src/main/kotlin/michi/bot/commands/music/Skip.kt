package michi.bot.commands.music

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.database.dao.GuildDAO
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.awt.Color

val guildSkipPoll = HashMap<Guild, MutableSet<User>>()

object Skip: MichiCommand("skip", GUILD_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Starts a poll to skip the current music(skips right away if possible)",
            DiscordLocale.ENGLISH_UK to "Starts a poll to skip the current music(skips right away if possible)",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Cria uma votação para pular a música atual(pula direto caso possível)"
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val guild = context.guild!!
        val poll = guildSkipPoll.computeIfAbsent(guild) { mutableSetOf() }
        val musicManager = PlayerManager[guild]
        val playingTrack = musicManager.player.playingTrack

        if (!canHandle(context)) return

        val usersInTheVC = guild.selfMember.voiceState!!.channel!!.members.size

        poll += sender

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val musicSuccess: YamlMap = success["music"]!!
        val successEphemeral: YamlMap = getYML(sender).yamlMap["success_messages"]!!
        val musicSuccessEphemeral: YamlMap = successEphemeral["music"]!!
        val skipPollMessage = musicSuccessEphemeral.getText("skip_poll").split("\n")

        EmbedBuilder().apply {
            setColor(Color.MAGENTA)
            addField(String.format(skipPollMessage[0], playingTrack.info.title), String.format(skipPollMessage[1], poll.size, usersInTheVC/2), false)
        }.build().let(context::replyEmbeds).setEphemeral(true).queue()

        if (isSkippable(guild)) {
            poll.clear()

            GuildDAO.getMusicQueue(guild)?.replace(playingTrack.info.uri, "")?.let {
                GuildDAO.setMusicQueue(guild, it)
            }

            context.channel.sendMessage(String.format(musicSuccess.getText("skip"), playingTrack.info.title, Emoji.michiThumbsUp))
                .queue()
            musicManager.scheduler.nextTrack()
        }
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild!!
        val bot = guild.selfMember
        val sender = context.member!!
        val senderVoiceState = sender.voiceState!!
        val botVoiceState = bot.voiceState!!

        val err: YamlMap = getYML(sender.user).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val musicErr: YamlMap = err["music"]!!

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        if (!senderVoiceState.inAudioChannel()) {
            context.michiReply(String.format(musicErr.getText("user_not_in_vc"), Emoji.michiBlep))
            return false
        }

        if (!botVoiceState.inAudioChannel()) {
            context.michiReply(musicErr.getText("custom_bot_not_in_vc"))
            return false
        }

        if (senderVoiceState.channel != botVoiceState.channel) {
            context.michiReply(musicErr.getText("user_not_in_bot_vc"))
            return false
        }

        if (sender.user in guildSkipPoll[guild]!!) {
            context.michiReply(String.format(musicErr.getText("already_voted_skip"), Emoji.smolMichiAngry))
            return false
        }

        return true
    }

    fun isSkippable(guild: Guild): Boolean {
        val poll = guildSkipPoll[guild] ?: return false
        val vcMemberCount = guild.selfMember.voiceState?.channel?.members?.filter { !it.user.isBot }?.size ?: return false

        return poll.size >= vcMemberCount / 2
    }

}