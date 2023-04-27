package michi.bot

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import michi.bot.commands.admin.*
import michi.bot.commands.math.*
import michi.bot.commands.misc.*
import michi.bot.listeners.*
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import okhttp3.internal.http2.Http2Connection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

import michi.bot.commands.*
import michi.bot.commands.mail.Inbox
import michi.bot.commands.mail.Mail
import michi.bot.commands.mail.Read
import michi.bot.commands.mail.RemoveMail
import michi.bot.commands.music.*
import michi.bot.commands.music.dj.*
import michi.bot.commands.util.*
import michi.bot.database.DataBaseFactory
import net.dv8tion.jda.api.utils.cache.CacheFlag

val config: Dotenv = Dotenv.configure().load()
val perspectiveAPI: PerspectiveAPI = PerspectiveAPI.create(config["PERSPECTIVE_API_TOKEN"])

/**
 * Function that wakes up Michi
 * @author Slz
 */

fun main() {
   Michi()
}

/**
 * Main class, initialize Michi
 * @author Slz
 */
class Michi {
    companion object {
        lateinit var logsPath: String
            private set

        val commandList = mutableListOf<MichiCommand>()
    }
    init {
        val logger: Logger = LoggerFactory.getLogger(Michi::class.java)

        // ideas from: https://github.com/MrGaabriel/Ayla/blob/master/src/main/kotlin/com/github/mrgaabriel/ayla/AylaLauncher.kt <3
        val configFile = File(".env")
        val logsDir = File("logs")

        if (!logsDir.exists()) {
            logsDir.mkdir()
            logsPath = logsDir.path
            val testLog = File("${logsDir.path}\\testLog.md")
            testLog.createNewFile()

            testLog.printWriter().use {
                it.println("# This is a test file.\n If you are seeing this, this means that everything is ok with the log folder.")
                it.println("`You can delete this file if you want.`\n One last thing: Good luck, I hope you have flawless experience <3")
            }

            logger.info("logs folder created.")
        }

        if (!configFile.exists()) {
            configFile.createNewFile()
            logger.info("Looks like you are trying to boot michi for the first time.\n You must configure her in the file \".env\"\nFollow the example in the file \"example.env\"")
            exitProcess(Status.INFO)
        }

        logger.info("configs loaded")
        val token = config["TOKEN"]

        // Register Commands
        commandList.add(Ban)
        commandList.add(UnBan)
        commandList.add(Clear)
        commandList.add(Mute)
        commandList.add(SlowMode)
        commandList.add(Raccoon)
        commandList.add(Wiki)
        commandList.add(Math)
        commandList.add(Ping)

        // Initializing JDA
        val michi = DefaultShardManagerBuilder.createDefault(token)

        // Activity
        michi.setActivity(Activity.watching("Brand New Animal!"))

        // Event listeners
        .addEventListeners(
            MessageListener,
            OnGuildReadyListener,
            SlashCommandListener,
            ModalInteractionListener,
            ButtonListener,
            CommandAutoCompletionListener,
            OnReadyListener
        )

        // Gateway intents
        .enableIntents(
            GatewayIntent.MESSAGE_CONTENT,
        )

        // Build
        .build()

        // Logger message
        logger.info("Michi is ready!")
    }

}
