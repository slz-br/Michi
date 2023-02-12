package michi.bot.util

import michi.bot.commands.admin.ban
import michi.bot.commands.admin.unban
import michi.bot.commands.math.MathLogic
import michi.bot.commands.math.MathProblem
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

abstract class CheckPossibility {

    companion object {

        fun checkMath(context: SlashCommandInteractionEvent) {
            val sender = context.user

            MathLogic.instances.forEach {
                if (sender == it.problemInstance.user) {
                    context.reply("Solve one problem before calling another ${Emoji.smolMichiAngry}")
                        .setEphemeral(true)
                        .queue()
                    return
                }
            }
            MathLogic.instances.add(MathLogic(MathProblem(sender), context))
        }

        fun checkBan(context: SlashCommandInteractionEvent) {
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
            ban(context, context.getOption("reason")?.asString, *subjects.toTypedArray())
        }

        fun checkUnban(context: SlashCommandInteractionEvent) {
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

            unban(context, *usersToUnban.toTypedArray())
        }

        private fun locateUserInGuild(guild: Guild, user: User): Boolean {

            var userNotFound = false

            guild.retrieveMember(user).queue(null) { userNotFound = true }
            if (userNotFound) return false
            return true
        }

    }
}