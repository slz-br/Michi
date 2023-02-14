package michi.bot.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import michi.bot.commands.admin.*
import michi.bot.commands.math.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

private const val DELAY = 7500L

abstract class CommandManager {

    companion object {
        private val coolDown = mutableListOf<User>()

        fun checkMath(context: SlashCommandInteractionEvent) {
            val sender = context.user

            MathLogic.instances.forEach {
                if (sender == it.problemInstance.user) {
                    context.reply("Solve one problem before calling another ${Emoji.smolMichiAngry}")
                        .setEphemeral(true)
                        .queue()
                    return
                }
                if(checkCooldown(sender, context)) return
            }
            MathLogic.instances.add(MathLogic(MathProblem(sender), context))
        }

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
                CoroutineScope(Dispatchers.IO).launch { coolDownManager(sender) }
                if (checkCooldown(sender, context))
                subjects.add(subject.asMember!!)
            }
            ban(context, context.getOption("reason")?.asString, *subjects.toTypedArray())
        }

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
                CoroutineScope(Dispatchers.IO).launch { coolDownManager(sender) }
                if (checkCooldown(sender, context))

                usersToUnban.add(subject.asUser)
            }

            unban(context, *usersToUnban.toTypedArray())
        }

        private fun locateUserInGuild(guild: Guild, user: User): Boolean {
            var userNotFound = false

            guild.retrieveMember(user).queue(null) { userNotFound = true }
            if (userNotFound) return false
            return true
        }

        private suspend fun coolDownManager(user: User) {
            coolDown.add(user)
            delay(DELAY)
            coolDown.remove(user)
        }

        private fun checkCooldown(sender: User, context: SlashCommandInteractionEvent): Boolean {
            if (coolDown.contains(sender)) {
                context.reply("you are in cooldown, wait a bit")
                    .setEphemeral(true)
                    .queue()
                return true
            }
            return false
        }

    }

}