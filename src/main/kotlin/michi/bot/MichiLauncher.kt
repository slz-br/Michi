package michi.bot

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI
import ch.qos.logback.core.status.Status
import io.github.cdimascio.dotenv.Dotenv
import michi.bot.commands.CommandNotImplemented
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess
import java.util.concurrent.LinkedBlockingDeque

import michi.bot.listeners.*
import michi.bot.database.DataBaseFactory
import michi.bot.commands.MichiCommand

val config: Dotenv = Dotenv.configure().load()
val perspectiveAPI: PerspectiveAPI = PerspectiveAPI.create(config["PERSPECTIVE_API_TOKEN"])

/**
 * Function that wakes up Michi
 * @author Slz
 */

suspend fun main() {
    // initializing PostgresSQL
    DataBaseFactory.init()

    // instantiating michi
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
        val logger = LoggerFactory.getLogger(Michi::class.java)

        // ideas from:
        // https://github.com/MrGaabriel/Ayla/blob/master/src/main/kotlin/com/github/mrgaabriel/ayla/AylaLauncher.kt <3
        val configFile = File(".env")
        val logsDir = File("logs")

        if (!logsDir.exists()) {
            logsDir.mkdir()
            logsPath = logsDir.path
            val testLog = File("$logsPath\\testLog.md")
            testLog.createNewFile()

            testLog.printWriter().use {
                it.println("# This is a test file.\n If you are seeing this, this means that everything is ok with the log folder.")
                it.println("`You can delete this file if you want.`\n One last thing: Good luck, I hope you have flawless experience <3")
            }

            logger.info("logs folder created.")
        }

        if (!configFile.exists()) {
            configFile.createNewFile()
            logger.info("Looks like you are trying to boot Michi for the first time.\n You must configure her in the file \".env\"\nFollow the example in the file \"example.env\"")
            exitProcess(Status.INFO)
        }

        logger.info("configs loaded")
        val token = config["TOKEN"]

        // Initializing connection with Discord
        DefaultShardManagerBuilder.createDefault(token).apply {

            // Activity
            setActivity(Activity.watching("Brand New Animal"))

            // Event listeners
            addEventListeners(
                MessageListener,
                OnGuildReadyListener,
                SlashCommandListener,
                ButtonListener,
                CommandAutoCompletionListener,
                OnReadyListener,
                OnGuildJoin,
                OnGuildLeaveListener,
                LogsListener
            )

            // Gateway intents
            enableIntents(
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_VOICE_STATES
            )

            // Enable cache
            enableCache(
                CacheFlag.VOICE_STATE
            )

        }.build()

        println(
            """
             __  __ ___ ___ _  _ ___ 
            |  \/  |_ _/ __| || |_ _|
            | |\/| || | (__| __ || | 
            |_|  |_|___\___|_||_|___|
            """.trimIndent()
        )

        registerCommands()
    }

    private fun registerCommands() {
        val commandsDir = File("${System.getProperty("user.dir")}\\build\\classes\\kotlin\\main\\michi\\bot\\commands")
        val allCommandsDir = searchCommandDirectories(commandsDir)

        allCommandsDir.forEach { currentDir ->
            currentDir.listFiles()?.forEach fileLoop@{ file ->
                if (!file.exists() || !file.name.endsWith(".class") || file.name.contains('$')) return@fileLoop

                val packageName = currentDir.canonicalPath.split("main\\")[1]

                val qualifiedName = "$packageName${file.canonicalPath.removePrefix(currentDir.canonicalPath)}"
                    .dropLast(6)
                    .replace('\\', '.')

                val commandClass = Class.forName(qualifiedName)

                if (
                    !MichiCommand::class.java.isAssignableFrom(commandClass)
                    || commandClass.isAnnotationPresent(CommandDeactivated::class.java)
                ) return@fileLoop

                val cmdConstructor = commandClass.asSubclass(MichiCommand::class.java).getDeclaredConstructor()
                cmdConstructor.isAccessible = true
                commandList.add(cmdConstructor.newInstance())
            }
        }
    }

    private fun searchCommandDirectories(initialDir: File): Set<File> {
        if (!initialDir.isDirectory || !initialDir.exists()) return emptySet()

        val commandDirectoriesList = mutableSetOf<File>()

        val visited = mutableSetOf(initialDir)
        val deque = LinkedBlockingDeque<File>().apply { add(initialDir) }

        while (deque.isNotEmpty()) {
            val dir = deque.pop()
            dir.listFiles()!!.forEach {
                if (!it.isDirectory || !it.exists() || visited.contains(it)) return@forEach

                commandDirectoriesList.add(it)
                visited.add(it)
                deque.add(it)
            }
        }

        return commandDirectoriesList
    }

}