package michi.bot.commands.math

import kotlinx.coroutines.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.util.LinkedList

/**
 * Gives the user a math problem to the user solve when the user uses the "math" SlashCommand, it also
 * counts the time that it took to the user solve the problem and cancels itself if the user takes longer than
 * 30 seconds to answer.
 * @author Slz
 */

class MathLogic(problem: MathProblem, event: SlashCommandInteractionEvent) {

    val initialTime: Long
    val problemInstance = problem
    var timeEndedUp: Boolean = false
    val context = event
    companion object {
        val instances = LinkedList<MathLogic>()
    }

    init {

        // making the embed
        val embed = EmbedBuilder()
        embed.setColor(Color.GREEN)
            .setTitle("**${problem.problemAsString}**")
            .setFooter("Solve the problem as quickly as you can!")

        // sending the embed
        context.replyEmbeds(embed.build()).queue()

        // Counting the time
        initialTime = System.currentTimeMillis()

        CoroutineScope(Dispatchers.IO).launch {
            checkDelay(problemInstance, context.channel.asTextChannel())
        }

    }

    private suspend fun checkDelay(problem: MathProblem, channel: TextChannel) {
        delay(45000L)
        if (!problemInstance.isAnswered) {
            channel.sendMessage("${problem.user.name} couldn't solve the problem in time.").queue()
            if (instances.contains(this)) {
                instances.remove(this)
            }
        }

    }

}