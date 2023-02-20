package michi.bot.commands.math

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import michi.bot.util.Emoji

/**
 * Checks if the answer that the user gave matches the user's problem instance result.
 * @param event the message event from the user.
 * @param mathLogicInstance the user's math problem instance.
 * @author Slz
 */
 fun checkAnswer(event: MessageReceivedEvent, mathLogicInstance: MathLogic) {

    val context: MessageReceivedEvent = event
    val msg: Int = context.message.contentRaw.toInt()
    val userName = context.author.name

    // guard clause
    if (mathLogicInstance.problemInstance.isAnswered || mathLogicInstance.timeEndedUp) return

    if (msg == mathLogicInstance.problemInstance.result) {
        val finalTime = (System.currentTimeMillis() - mathLogicInstance.initialTime) / 1000
        context.channel.sendMessage("**Correct** $userName ${Emoji.michiYesCushion}\nTime: ${finalTime}s")
            .queue()
        mathLogicInstance.problemInstance.isAnswered = true
    }

    else {
        context.channel.sendMessage("**Wrong** $userName ${Emoji.michiGlare}\nAnswer: ${mathLogicInstance.problemInstance.result}")
            .queue()
        mathLogicInstance.problemInstance.isAnswered = true
    }
    if(MathLogic.instances.contains(mathLogicInstance)) MathLogic.instances.remove(mathLogicInstance)
}
