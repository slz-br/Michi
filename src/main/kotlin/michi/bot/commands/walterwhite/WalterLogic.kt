package michi.bot.commands.walterwhite

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.io.BufferedReader
import java.io.FileReader
import java.util.Random


/**
 * Sends a random walter white image when called.
 * @throws NoSuchFileException
 * @author Slz
 */

    fun sendRandomWalterWhiteImage(context: MessageReceivedEvent) {

        val rng = Random()
        val options = BufferedReader(FileReader("walter.txt")).readLines()
        val imageURL = options[rng.nextInt(options.size)]

        //sending the image
        context.message.reply(imageURL).queue()

    }


