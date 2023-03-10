package michi.bot.commands.math

import michi.bot.util.Emoji
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.util.Random

/**
 * Creates a math problem with a random operation and numbers
 * operations: sum(+), subtraction(-), multiplication(*)
 * @param sender The user that will need to solve this math problem.
 * @author Slz
 */

class MathProblem(sender: User) {
    val problemAsString: String
    val result: Int
    var isAnswered = false
    val user: User

    companion object {

        /**
         * Checks if the user that sent the slashCommand already has an active mathProblem to solve.
         * @param context The SlashCommandInteractionEvent that called the math function.
         * @author Slz
         */
        fun tryToExecute(context: SlashCommandInteractionEvent) {
            val sender = context.user

            MathProblemManager.instances.forEach {
                if (sender == it.problemInstance.user) {
                    context.reply("Solve one problem before calling another ${Emoji.smolMichiAngry}")
                        .setEphemeral(true)
                        .queue()
                    return
                }
            }
            MathProblemManager.instances.add(MathProblemManager(MathProblem(sender), context))
        }

    }

    init {
        user = sender
        val rng = Random()
        val x = rng.nextInt(100)
        val y = rng.nextInt(100)

        when (rng.nextInt(3)) {
            0 -> {
                result =  sum(x, y)
                problemAsString = "$x + $y"
            }
            1 -> {
                result = subtract(x, y)
                problemAsString = "$x - $y"
            }
            else -> {
                result = multiply(x, y)
                problemAsString = "$x * $y"
            }
        }
    }

    /**
     * Function to get the result of a sum math problem.
     * @return The result of x + y
     * @author Slz
     */
    private fun sum(x: Int, y: Int): Int = x + y

    /**
     * Function to get the result of a subtraction math problem.
     * @return The result of x - y
     * @author Slz
     */
    private fun subtract(x: Int, y: Int): Int = x - y

    /**
     * Function to het the result of a multiplication math problem.
     * @return The result of x * y
     * @author Slz
     */
    private fun multiply(x: Int, y: Int): Int = x * y

}