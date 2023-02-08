package michi.bot.commands.math

import net.dv8tion.jda.api.entities.User
import java.util.Random

/**
 * Creates a math problem with a random operation and numbers
 * operations: sum(+), subtraction(-), multiplication(*)
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

    private fun sum(x: Int, y: Int): Int {
        return x + y
    }

    private fun subtract(x: Int, y: Int): Int {
        return x - y
    }

    private fun multiply(x: Int, y: Int): Int {
        return x * y
    }

}