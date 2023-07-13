package michi.bot.listeners

import kotlinx.coroutines.*
import michi.bot.commands.music.Skip
import michi.bot.commands.music.guildSkipPoll
import michi.bot.database.dao.GuildsDAO
import michi.bot.lavaplayer.PlayerManager
import michi.bot.util.Emoji
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
 * Multiple event listeners dedicated for log purposes.
 * @author Slz
 */
object LogsListener: ListenerAdapter() {

    override fun onGuildUpdateBanner(event: GuildUpdateBannerEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val newBannerURL = event.newBannerUrl
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField("Server Banner changed!", "banner:", false)
                setImage(newBannerURL)
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildUpdateDescription(event: GuildUpdateDescriptionEvent)  {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val oldDescription = event.oldDescription
            val newDescription = event.newDescription

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(
                    "Server description changed!",
                    "old description: $oldDescription\nnew description: $newDescription",
                    false
                )
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildUpdateBoostTier(event: GuildUpdateBoostTierEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val tier = event.newBoostTier

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField("Server boost tier changed!", "tier: $tier", false)
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildUpdateBoostCount(event: GuildUpdateBoostCountEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val boostCount = event.newBoostCount

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField("Server boost count changed!", "count: $boostCount", false)
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildUpdateName(event: GuildUpdateNameEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val oldName = event.oldName
            val newName = event.newName

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField("Server name changed!", "old name: $oldName\nnew name: $newName", false)
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildBan(event: GuildBanEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val bannedUser = event.user

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField("User was banned!", "user: ${bannedUser.asMention}", false)
                setThumbnail(bannedUser.avatarUrl)
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildUnban(event: GuildUnbanEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val unbannedUser = event.user

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField("User unbanned!", "user: ${unbannedUser.asMention}", false)
                setThumbnail(unbannedUser.avatarUrl)
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildMemberUpdateTimeOut(event: GuildMemberUpdateTimeOutEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val mutedUser = event.user
            val mutedUntil = event.newTimeOutEnd

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(
                    "User muted",
                    "user: ${mutedUser.asMention}\nmuted until: ${mutedUntil ?: "unknow ${Emoji.michiThink}"}",
                    false
                )
                setThumbnail(mutedUser.avatarUrl)
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent)  {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel: TextChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val memberRemoved = event.user

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField("User kicked", "user: ${memberRemoved.asMention}", false)
                setThumbnail(memberRemoved.avatarUrl)
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onRoleCreate(event: RoleCreateEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel: TextChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val role = event.role

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField("Role created!", "name: ${role.name}\nposition: ${role.position}", false)
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onRoleDelete(event: RoleDeleteEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val role = event.role

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField("Role deleted!", "name: ${role.name}", false)
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val member = event.member
            val memberVoiceState = event.voiceState

            val embed = EmbedBuilder().apply {
                setColor(Color.BLACK)
                setThumbnail(member.avatarUrl)
                setFooter(guild.name, guild.iconUrl)
            }

            if (memberVoiceState.inAudioChannel()) {
                embed.apply {
                    addField(
                        "Member joined a voice channel!",
                        "${member.asMention} Joined ${memberVoiceState.channel?.asMention ?: "a voice channel"}",
                        false
                    )
                }
            }
            else {
                embed.apply {
                    addField(
                        "Member left a voice channel",
                        "${member.asMention} left ${memberVoiceState.channel?.asMention ?: "a voice channel"}",
                        false
                    )
                }
                val scheduler = PlayerManager.getMusicManager(guild).scheduler
                if (Skip.isSkippable(guild)) {
                    val playingTrack = PlayerManager.getMusicManager(guild).playingTrack

                    playingTrack?.let {
                        val newMusicQueue = GuildsDAO.getMusicQueue(guild)?.replace("${playingTrack.info.uri},", "")
                        GuildsDAO.setMusicQueue(guild, newMusicQueue)


                        guildSkipPoll[guild]!!.clear()
                        scheduler.nextTrack()
                        event.channelLeft?.asVoiceChannel()
                            ?.sendMessage("Skipped ${playingTrack.info.title} ${Emoji.michiThumbsUp}")
                            ?.queue()
                    }
                }
            }

            logsChannel.sendMessageEmbeds(embed.build())
                .queue()
        }
    }

    override fun onChannelCreate(event: ChannelCreateEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val channel = event.channel

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField("Channel created!", "channel: ${channel.asMention}\ntype: ${channel.type.name}", false)
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onChannelDelete(event: ChannelDeleteEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val channel = event.channel

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField("Channel deleted!", "channel name: ${channel.name}\ntype: ${channel.type.name}", false)
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildMemberUpdateAvatar(event: GuildMemberUpdateAvatarEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(
                    "User changed avatar!",
                    "New avatar: ${event.newAvatar}\n Old avatar: ${event.oldAvatar}",
                    false
                )
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

        }
    }

    override fun onGuildUpdateOwner(event: GuildUpdateOwnerEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val guild = event.guild
            val logsChannelID = GuildsDAO.getLogsChannel(guild) ?: return@launch
            val logsChannel = guild.getTextChannelById(logsChannelID) ?: return@launch
            val newOwner = event.newOwner ?: return@launch

            EmbedBuilder().apply {
                setColor(Color.BLACK)
                addField(
                    "Server owner changed!",
                    "new owner: $newOwner\nold owner: ${event.oldOwner}",
                    false
                )
                setFooter(guild.name, guild.iconUrl)
            }.build().let(logsChannel::sendMessageEmbeds).queue()

            GuildsDAO.setOwner(guild, newOwner)
        }
    }

}