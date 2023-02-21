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
 * @param problem The math problem to manage.
 * @param event The slashCommandInteraction that called the math command.
 * @author Slz
 */

class MathProblemManager(problem: MathProblem, event: SlashCommandInteractionEvent) {

    val initialTime: Long
    val problemInstance = problem
    var timeEndedUp: Boolean = false
    val context = event
    companion object {
        val instances = LinkedList<MathProblemManager>()
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

    /**
     * Checks if the math problem was solved after the delay.
     * @param problem The math problem to check
     * @param channel The channel to send the message if the problem wasn't answered in time.
     * @author Slz
     */
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