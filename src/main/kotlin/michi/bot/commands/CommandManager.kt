package michi.bot.commands

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import michi.bot.commands.admin.ban
import michi.bot.commands.admin.unban
import michi.bot.commands.math.MathProblemManager
import michi.bot.commands.math.MathProblem
import michi.bot.commands.misc.raccoon.randomRaccoon
import michi.bot.commands.misc.wikipedia.randomWiki
import michi.bot.commands.music.MusicCommands
import michi.bot.util.Emoji
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.net.URI
import java.net.URISyntaxException

private const val DELAY = 10000L

/**
 * Checks if it is possible to execute a command and manages the users in cooldown.
 * @author Slz
 */

abstract class CommandManager {
    companion object {
        private val coolDown = mutableListOf<User>()

        /**
         * Checks if the user that sent the slashCommand already has an active mathProblem to solve.
         * @param context The SlashCommandInteractionEvent that called the math function.
         * @author Slz
         */
        fun checkMath(context: SlashCommandInteractionEvent) {
            val sender = context.user

            MathProblemManager.instances.forEach {
                if (sender == it.problemInstance.user) {
                    context.reply("Solve one problem before calling another ${Emoji.smolMichiAngry}")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                if(checkCooldown(sender, context)) return
            }
            MathProblemManager.instances.add(MathProblemManager(MathProblem(sender), context))
        }

        /**
         * Checks if all users to ban are in the guild.
         * @param context the slashCommandInteractionEvent that called the ban command.
         * @see checkUnban
         * @author Slz
         */
        fun checkBan(context: SlashCommandInteractionEvent) {
            val sender = context.user
            val options = context.options
            val guild = context.guild!!
            val subjects = mutableListOf<Member>()

            for (subject in options) {
                if (subject.type.name != "USER") continue

                if (!locateUserInGuild(guild, subject.asUser)) {
                    context.reply("${subject.asUser.name} not found(User isn't in the server? ${Emoji.michiThink}")
                        .setEphemeral(true)
                        .queue()
                    return
                }

                subjects.add(subject.asMember!!)
            }

            if (checkCooldown(sender, context)) return
            CoroutineScope(Dispatchers.IO).launch {
                coolDownManager(sender)
            }
            ban(context, context.getOption("reason")?.asString, *subjects.toTypedArray())

        }

        /**
         * Checks if the users to unban are actually banned.
         * @param context The slashCommandInteractionEvent that called the unban command.
         * @author Slz
         * @see checkBan
         */
        fun checkUnban(context: SlashCommandInteractionEvent) {
            val sender = context.user
            val options = context.options
            val guild = context.guild!!
            val usersToUnban = mutableListOf<User>()

            for (subject in options) {

                if (subject.type.name != "USER") continue

                if (!locateUserInGuild(guild, subject.asUser)) {
                    context.reply("${subject.asUser.name} not found(User isn't in the server? ${Emoji.michiThink}")
                        .setEphemeral(true)
                        .queue()
                    return
                }

                usersToUnban.add(subject.asUser)
            }
            if (checkCooldown(sender, context)) return
            CoroutineScope(Dispatchers.IO).launch {
                coolDownManager(sender)
            }
            unban(context, *usersToUnban.toTypedArray())
        }

        /**
         * Checks if the bot can connect to a voice channel.
         * @param context The SlashCommandInteraction that called the join or play command.
         * @author Slz
         */
        fun canConnect(context: SlashCommandInteractionEvent): Boolean {
            val bot = context.guild!!.selfMember
            val botVoiceState = bot.voiceState!!

            if (botVoiceState.inAudioChannel()) {
                context.reply("I'm already in a voice channel")
                    .setEphemeral(true)
                    .queue()
                return false
            }

            val executorVoiceState = context.member!!.voiceState!!
            if (!executorVoiceState.inAudioChannel()) {
                context.reply("You need to be in a channel to use this command, silly you ${Emoji.michiBlep}")
                    .setEphemeral(true)
                    .queue()
                return false
            }

            val channelToJoin = executorVoiceState.channel!!

            if (!bot.hasAccess(channelToJoin.asVoiceChannel())) {
                context.reply("I can't join this channel ${Emoji.michiSad}")
                    .setEphemeral(true)
                    .queue()
                return false
            }

            return true
        }

        /**
         * Checks if it is possible to play a track.
         * @param context The SlashCommandInteractionEvent that called the play command.
         * @author Slz
         * @see canConnect
         */
        fun checkPlay(context: SlashCommandInteractionEvent) {
            val bot = context.guild!!.selfMember
            val botVoicestate = bot.voiceState!!
            val option = context.options
            val audioManager = context.guild!!.audioManager
            val channelToConnect = context.member!!.voiceState!!.channel
            val sender = context.user

            if(!botVoicestate.inAudioChannel()) {
                if (!canConnect(context)) return
                audioManager.isSelfDeafened = true
                audioManager.openAudioConnection(channelToConnect)
            }

            var url = option[0].asString
            if (!isUrl(url)) {
                url = "ytsearch: $url"
            }

            if (checkCooldown(sender, context)) return
            CoroutineScope(Dispatchers.IO).launch {
                coolDownManager(sender)
            }
            MusicCommands.play(context, url)
        }

        /**
         * Checks if the user is in cooldown to use the randomWiki function.
         * @param context The SlashCommandInteractionEvent that called the wiki function.
         * @author Slz
         */
        fun checkWiki(context: SlashCommandInteractionEvent) {
            val sender = context.user

            if (checkCooldown(sender, context)) return
            CoroutineScope(Dispatchers.IO).launch {
                coolDownManager(sender)
            }
            randomWiki(context)
        }

        fun checkRaccoon(context: SlashCommandInteractionEvent) {
            val sender = context.user

            if (checkCooldown(sender, context)) return
            CoroutineScope(Dispatchers.IO).launch {
                coolDownManager(sender)
            }
            randomRaccoon(context)
        }

        /**
         * Checks if a user meets the requirements to use the skip command.
         * @param context The SlashCommandInteraction that called the skip function.
         * @author Slz
         * @see checkStop
         */
        fun checkSkip(context: SlashCommandInteractionEvent) {
            val sender = context.user
            val senderVoiceState = context.member!!.voiceState!!
            val botVoiceState = context.guild!!.selfMember.voiceState!!

            if (!botVoiceState.inAudioChannel()) {
                context.reply("I need to be in a voice channel.")
                    .setEphemeral(true)
                    .queue()
                return
            }

            if (!senderVoiceState.inAudioChannel()) {
                context.reply("You need to be in a voice channel to use this command.")
                    .setEphemeral(true)
                    .queue()
                return
            }

            if (senderVoiceState.channel != botVoiceState.channel) {
                context.reply("You need to be in the same voice channel as me to use this command.")
                    .setEphemeral(true)
                    .queue()
                return
            }

            if(checkCooldown(sender, context)) return

            MusicCommands.skip(context)
        }

        fun checkStop(context: SlashCommandInteractionEvent) {
            val sender = context.member!!
            val bot = context.guild!!.selfMember
            val botVoiceState = bot.voiceState!!

            val senderVoiceState = context.member!!.voiceState!!

            if(!botVoiceState.inAudioChannel()) {
                context.reply("I need to be in a voice channel.")
                    .setEphemeral(true)
                    .queue()
                return
            }

            if (!senderVoiceState.inAudioChannel()) {
                context.reply("You need to be in a channel to use this command, silly you ${Emoji.michiBlep}")
                    .setEphemeral(true)
                    .queue()
                return
            }

            if(senderVoiceState.channel != botVoiceState.channel) {
                context.reply("You need to be in the same channel as me to use this command")
                    .setEphemeral(true)
                    .queue()
                return
            }

            if (!sender.hasPermission(Permission.ADMINISTRATOR)) {
                context.reply("Only admins can use this command")
                    .setEphemeral(true)
                    .queue()
                return
            }

            if (checkCooldown(sender.user, context)) return
            CoroutineScope(Dispatchers.IO).launch {
                coolDownManager(sender.user)
            }
            MusicCommands.stop(context)

        }

        /**
         * Tries to retrieve a User from a guild.
         * @param guild The guild to look for the user;
         * @param user The user to search on the guild.
         * @return True if the user was found in the guild, false if not.
         * @author Slz
         */
        private fun locateUserInGuild(guild: Guild, user: User): Boolean {
            var userNotFound = false

            guild.retrieveMember(user).queue(null) { userNotFound = true }
            if (userNotFound) return false
            return true
        }

        /**
         * Puts the user in the cooldown list and remove him from the list after
         * the delay.
         * @param user the user to put in cooldown
         * @author Slz
         */
        private suspend fun coolDownManager(user: User) {
            coolDown.add(user)
            delay(DELAY)
            coolDown.remove(user)
        }

        /**
         * Checks if a user is in cooldown.
         * @param sender The user that sent the command.
         * @param context The slashCommandInteractionEvent that called a function.
         * @return True if the user is in cooldown, false if not.
         * @author Slz
         */
        private fun checkCooldown(sender: User, context: SlashCommandInteractionEvent): Boolean {
            if (coolDown.contains(sender)) {
                context.reply("You are in cooldown, wait a bit")
                    .setEphemeral(true)
                    .queue()
                return true
            }
            return false
        }

        /**
         * Checks if a string is an Url.
         * @param possibleUrl The string to test if it is an Url.
         * @return True if it is an Url, false if not.
         * @author Slz
         */
        private fun isUrl(possibleUrl: String): Boolean {
            return try {
                URI(possibleUrl.trim())
                true
            } catch (e: URISyntaxException) {
                false
            }
        }

    }

}