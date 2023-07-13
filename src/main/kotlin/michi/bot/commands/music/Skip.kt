package michi.bot.commands.music

import michi.bot.commands.CommandScope
import michi.bot.commands.MichiCommand
import michi.bot.database.dao.GuildsDAO
import michi.bot.lavaplayer.PlayerManager
import michi.bot.listeners.SlashCommandListener
import michi.bot.util.Emoji
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

val guildSkipPoll = HashMap<Guild, MutableSet<User>>()

object Skip: MichiCommand("skip", "Starts a poll to skip the current music(skips right away if possible)", CommandScope.GUILD_SCOPE) {

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        val guild = context.guild!!
        val poll = guildSkipPoll.computeIfAbsent(guild) { mutableSetOf() }
        val musicManager = PlayerManager.getMusicManager(guild)
        val playingTrack = musicManager.player.playingTrack

        if (!canHandle(context)) return

        val usersInTheVC = guild.selfMember.voiceState!!.channel!!.members.size

        poll.add(sender)

        EmbedBuilder().apply {
            setColor(Color.MAGENTA)
            addField("Poll to skip ${playingTrack.info.title}", "${poll.size} of ${usersInTheVC/2} needed voted to skip", false)
        }.build().let(context::replyEmbeds).queue()

        if (isSkippable(guild)) {
            poll.clear()

            val newMusicQueue = GuildsDAO.getMusicQueue(guild)?.replace(playingTrack.info.uri, "")
            GuildsDAO.setMusicQueue(guild, newMusicQueue)

            context.channel.sendMessage("Skipped ${playingTrack.info.title} ${Emoji.michiThumbsUp}")
                .queue()
            musicManager.scheduler.nextTrack()
        }

        SlashCommandListener.cooldownManager(sender)
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild!!
        val bot = guild.selfMember
        val sender = context.member!!
        val senderVoiceState = sender.voiceState!!
        val botVoiceState = bot.voiceState!!

        if (!bot.permissions.containsAll(botPermissions)) {
            context.reply("I don't have the permissions to execute this command ${Emoji.michiSad}")
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
            context.reply("I need to be in a VC for you to use this command.")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (senderVoiceState.channel != botVoiceState.channel) {
            context.reply("You need to be in the same voice channel as me to use this command")
                .setEphemeral(true)
                .queue()
            return false
        }

        if (guildSkipPoll[guild]!!.contains(sender.user)) {
            context.reply("You already voted to skip ${Emoji.smolMichiAngry}")
                .setEphemeral(true)
                .queue()
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