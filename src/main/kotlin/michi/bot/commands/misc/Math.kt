package michi.bot.commands.misc

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import michi.bot.commands.CommandScope.GLOBAL_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.awt.Color
import java.util.*

@Suppress("Unused")
object Math: MichiCommand("math", GLOBAL_SCOPE) {

    /**
     * Creates a math problem for the user if possible.
     * @param context The interaction to retrieve info from.
     * @author Slz
     * @see canHandle
     */
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val sender = context.user
        if (!canHandle(context)) return

        MathProblemManager.instances += MathProblemManager(MathProblem(sender), context)
    }

    /**
     * Checks if the user that sent the slashCommand already has an active mathProblem to solve.
     * @param context The SlashCommandInteractionEvent that called the math function.
     * @author Slz
     * @see execute
     */
    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val guild = context.guild

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val miscErr: YamlMap = err["misc"]!!

        MathProblemManager.instances.forEach {
            if (sender != it.problemInstance.user) return@forEach

            context.michiReply(String.format(miscErr.getText("user_already_has_math_problem"), Emoji.smolMichiAngry))
            return false
        }

        guild?.run {
            val bot = guild.selfMember

            if (!bot.permissions.containsAll(botPermissions)) {
                context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
                return false
            }

        }

        return true
    }

}

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

    init {
        user = sender
        val rng = Random()
        val x = rng.nextInt(100)
        val y = rng.nextInt(100)

        when (rng.nextInt(3)) {
            0 -> {
                result = sum(x, y)
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
    private fun sum(x: Int, y: Int) = x + y

    /**
     * Function to get the result of a subtraction math problem.
     * @return The result of x - y
     * @author Slz
     */
    private fun subtract(x: Int, y: Int) = x - y

    /**
     * Function to het the result of a multiplication math problem.
     * @return The result of x * y
     * @author Slz
     */
    private fun multiply(x: Int, y: Int) = x * y

}

/**
 * Gives the user a math problem for the user solve. It also
 * counts the time that it took to the user solve the problem and cancels itself if the user takes longer than
 * 30 seconds to answer.
 * @param problem The math problem to manage.
 * @param event The slashCommandInteraction that called the math command.
 * @author Slz
 * @see MathProblem
 */

class MathProblemManager(problem: MathProblem, event: SlashCommandInteractionEvent) {

    private val initialTime: Long
    val problemInstance = problem
    private var timeEndedUp: Boolean = false
    val context = event

    companion object {

        /**
         * LinkedList containing all the instances of MathProblemManager.
         * @author Slz
         */
        val instances = LinkedList<MathProblemManager>()

    }

    init {

        // making the embed
        val embed = EmbedBuilder()
        embed.setColor(Color.GREEN)
            .setTitle("**${problem.problemAsString}**")
            .setFooter("Solve the problem as quickly as you can!")

        // sending the embed
        context.michiReply(embed.build())

        // Counting the time
        initialTime = System.currentTimeMillis()

        CoroutineScope(IO).launch {
            checkDelay(problemInstance, context)
        }

    }

    /**
     * Checks if the math problem was solved after the delay.
     * @param problem The math problem to check.
     * @param context The context to reply if the problem wasn't answered in time.
     * @author Slz
     */
    private suspend fun checkDelay(problem: MathProblem, context: SlashCommandInteractionEvent) {
        delay(35000L)
        if (!problemInstance.isAnswered) {
            context.michiReply("${problem.user.name} couldn't solve the problem in time.")
            if (instances.contains(this)) instances.remove(this)
        }

    }

    /**
     * Checks if the answer that the user gave matches the user's problem instance result.
     * @param event the message event from the user.
     * @param mathLogicInstance the user's math problem instance.
     * @author Slz
     */
    fun checkAnswer(event: MessageReceivedEvent, mathLogicInstance: MathProblemManager) {
        val channel = event.channel
        val answer = event.message.contentRaw.toInt()
        val user = event.author.asMention

        // guard clause
        if (this.problemInstance.isAnswered || this.timeEndedUp) return

        if (answer == this.problemInstance.result) {
            val finalTime = (System.currentTimeMillis() - mathLogicInstance.initialTime) / 1000
            channel.sendMessage("**Correct** $user ${Emoji.michiYesCushion}\nTime: ${finalTime}s").queue()
            this.problemInstance.isAnswered = true
        } else {
            channel.sendMessage("**Wrong** $user ${Emoji.michiGlare}\nAnswer: ${this.problemInstance.result}").queue()
            this.problemInstance.isAnswered = true
        }

        if (instances.contains(mathLogicInstance)) instances -= mathLogicInstance
    }

}