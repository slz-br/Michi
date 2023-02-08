package michi.bot.listeners

import michi.bot.commands.admin.ban
import michi.bot.commands.math.MathLogic
import michi.bot.commands.math.MathProblem
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import michi.bot.util.Emoji

/**
 * Called whenever there's a SlashCommandInteractionEvent
 * @author Slz
 */

class SlashCommandListener: ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val name = event.name
        val sender = event.user
        val guild = event.guild
        val blackList = BufferedReader(FileReader("BlackList.txt")).readLines()

        if (!event.isFromGuild) return

        if (blackList.contains(sender.id) || blackList.contains(guild.id)) {
            event.reply("You can't use my commands anymore ${Emoji.michiTroll}")
                .setEphemeral(true)
                .queue()
            return
        }

        // if everything is right
        when (name) {
            "math" -> {
                MathLogic.instances.forEach {
                    if(sender == it.problemInstance.user) {
                        event.reply("Solve one problem before calling another ${Emoji.smolMichiAngry}")
                            .setEphemeral(true)
                            .queue()
                        return
                    }
                }
                MathLogic.instances.add(MathLogic(MathProblem(sender), event))
            }

            // it can only ban people that are in the server.
            "ban" -> {
                val subjects = mutableListOf<Member>()

                for (subject in event.options) {
                    if(subject.type.name != "USER" || subject.type.name == "STRING") continue

                    var hadError = false

                    event.guild!!.retrieveMember(subject.asUser).queue(
                        // success
                        {subjects.add(subject.asMember!!)},
                        // failure
                        {
                            hadError = true
                            event.reply("couldn't ban ${subject.asUser.name} (User isn't in the server?) ${Emoji.michiThink}")
                                .setEphemeral(true)
                                .queue()
                        }
                    )

                    if(hadError) return

                    subjects.add(subject.asMember!!)

                }

                ban(event, event.getOption("reason")?.asString, *subjects.toTypedArray())

            }

        }

    }
}