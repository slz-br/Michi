package michi.bot.listeners

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import michi.bot.commands.music.Skip
import michi.bot.commands.music.guildSkipPoll
import michi.bot.database.dao.GuildDAO
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent
import net.dv8tion.jda.api.events.guild.update.*
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.role.RoleCreateEvent
import net.dv8tion.jda.api.events.role.RoleDeleteEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color

/**
 * Multiple event listeners dedicated for logging purposes.
 * @author Slz
 */
object LogsListener: ListenerAdapter() {

    override fun onGuildUpdateBanner(event: GuildUpdateBannerEvent) {
        CoroutineScope(IO).launch {
            val newBannerURL = event.newBannerUrl
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("update_banner").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(logMessage[0], logMessage[1], false)
                setImage(newBannerURL)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildUpdateDescription(event: GuildUpdateDescriptionEvent)  {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("update_description").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(
                    logMessage[0],
                    String.format(logMessage[1] + logMessage[2], event.oldDescription, event.newDescription),
                    false
                )
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildUpdateBoostTier(event: GuildUpdateBoostTierEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("update_boost_tier").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(logMessage[0], String.format(logMessage[1], event.newBoostTier.name), false)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildUpdateBoostCount(event: GuildUpdateBoostCountEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch

            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("update_boost_count").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(logMessage[0], String.format(logMessage[1], event.newBoostCount), false)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildUpdateName(event: GuildUpdateNameEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("update_guild_name").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(
                    logMessage[0],
                    String.format(logMessage[1] + logMessage[2], event.oldName, event.newName),
                    false
                )
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildBan(event: GuildBanEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val bannedUser = event.user

            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("user_banned").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(logMessage[0], String.format(logMessage[1], bannedUser.asMention), false)
                setThumbnail(bannedUser.avatarUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildUnban(event: GuildUnbanEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val unbannedUser = event.user

            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("user_unbanned").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(logMessage[0], String.format(logMessage[1], unbannedUser.asMention), false)
                setThumbnail(unbannedUser.avatarUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildMemberUpdateTimeOut(event: GuildMemberUpdateTimeOutEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val mutedUser = event.user
            val mutedUntil = event.newTimeOutEnd

            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("user_timeouted").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(
                    logMessage[0],
                    String.format(logMessage[1] + logMessage[2], mutedUser.asMention, mutedUntil ?: "404 ${Emoji.michiThink}"),
                    false
                )
                setThumbnail(mutedUser.avatarUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent)  {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel: TextChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val memberKicked = event.user

            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("user_kicked").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(logMessage[0], String.format(logMessage[1], memberKicked.asMention), false)
                setThumbnail(memberKicked.avatarUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onRoleCreate(event: RoleCreateEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel: TextChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val role = event.role

            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("role_created").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(logMessage[0], String.format(logMessage[1] + logMessage[2], role.name, role.position), false)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onRoleDelete(event: RoleDeleteEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val role = event.role

            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("role_deleted").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(logMessage[0], String.format(logMessage[1], role.name), false)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val member = event.member
            val memberVoiceState = event.voiceState

            val embed = EmbedBuilder().apply {
                setColor(Color.BLACK)
                setThumbnail(member.avatarUrl)
            }

            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!

            if (memberVoiceState.inAudioChannel()) {
                val logMessage = logs.getText("joined_voice_channel").split("\n")

                embed.apply {
                    addField(
                        logMessage[0],
                        String.format(logMessage[1], member.asMention, event.channelJoined ?: "404 ${Emoji.michiThink}"),
                        false
                    )
                }
            }
            else {
                val logMessage = logs.getText("left_voice_channel").split("\n")
                embed.apply {
                    addField(
                        logMessage[0],
                        String.format(logMessage[1], member.asMention, event.channelLeft ?: "404 ${Emoji.michiThink}"),
                        false
                    )
                }
                val scheduler = PlayerManager[guild].scheduler
                if (Skip.isSkippable(guild)) {
                    val playingTrack = PlayerManager[guild].playingTrack

                    playingTrack?.let {
                        GuildsDAO.getMusicQueue(guild)?.replace("${playingTrack.info.uri},", "")?.let {
                            GuildsDAO.setMusicQueue(guild, it)
                        }

                        val success: YamlMap = getYML(event.guild).yamlMap["success_messages"]!!
                        val musicSuccess: YamlMap = success["music"]!!

                        guildSkipPoll[guild]!!.clear()
                        scheduler.nextTrack()
                        event.channelLeft?.asGuildMessageChannel()
                            ?.sendMessage(String.format(musicSuccess.getText("skip"), playingTrack.info.title, Emoji.michiThumbsUp))
                            ?.queue()
                    }
                }
            }

            logsChannel.sendMessageEmbeds(embed.build())
                .queue()
        }
    }

    override fun onChannelCreate(event: ChannelCreateEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val channel = event.channel

            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("channel_created").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(logMessage[0], String.format(logMessage[1] + logMessage[2], channel.name, channel.type.name), false)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onChannelDelete(event: ChannelDeleteEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val channel = event.channel

            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("channel_created").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(logMessage[0], String.format(logMessage[1] + logMessage[2], channel.name, channel.type.name), false)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildMemberUpdateAvatar(event: GuildMemberUpdateAvatarEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch

            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("user_updated_avatar").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(
                    logMessage[0],
                    String.format(logMessage[1] + logMessage[2], event.oldAvatar, event.oldAvatar),
                    false
                )
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildUpdateOwner(event: GuildUpdateOwnerEvent) {
        CoroutineScope(IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val newOwner = event.newOwner ?: return@launch

            val logs: YamlMap = getYML(event.guild).yamlMap["logs"]!!
            val logMessage = logs.getText("channel_created").split("\n")

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(
                    logMessage[0],
                    String.format(logMessage[1] + logMessage[2], newOwner, event.oldOwner),
                    false
                )
            }.build().let(logsChannel::sendMessageEmbeds).queue()

            GuildsDAO.setOwner(guild, newOwner)
        }
    }

}