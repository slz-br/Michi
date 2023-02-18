package michi.bot.commands.math

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import michi.bot.util.Emoji

/**
 * Checks if the answer that the user gave matches the user's problem instance result.
 */
     fun checkAnswer(event: MessageReceivedEvent, mathLogicInstance: MathLogic) {

        val context: MessageReceivedEvent = event
        val msg: Int = context.message.contentRaw.toInt()

        // guard clause
        if (mathLogicInstance.problemInstance.isAnswered || mathLogicInstance.timeEndedUp) return

        if (msg == mathLogicInstance.problemInstance.result) {
            val finalTime = (System.currentTimeMillis() - mathLogicInstance.initialTime) / 1000
            context.channel.sendMessage("**Correct** ${Emoji.michiYesCushion}\nTime: ${finalTime}s")
                .queue()
            mathLogicInstance.problemInstance.isAnswered = true
        }

        else {
            context.channel.sendMessage("**Wrong** ${Emoji.michiGlare}\nAnswer: ${mathLogicInstance.problemInstance.result}")
                .queue()
            mathLogicInstance.problemInstance.isAnswered = true
        }
        if(MathLogic.instances.contains(mathLogicInstance)) MathLogic.instances.remove(mathLogicInstance)
    }
