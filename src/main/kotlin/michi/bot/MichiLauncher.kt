package michi.bot

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI
import ch.qos.logback.core.status.Status
import io.github.cdimascio.dotenv.Dotenv
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import org.slf4j.*
import java.io.File
import kotlin.system.exitProcess
import java.util.concurrent.LinkedBlockingDeque
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import michi.bot.commands.CommandDeactivated
import michi.bot.listeners.*
import michi.bot.database.DataBaseFactory
import michi.bot.commands.MichiCommand
import net.dv8tion.jda.api.utils.MemberCachePolicy

val config: Dotenv = Dotenv.configure().load()
val perspectiveAPI: PerspectiveAPI = PerspectiveAPI.create(config["PERSPECTIVE_API_TOKEN"])

// todo: Implement the SetDj command
// todo: migrate to LavaLink.kt -> do after release
// todo: support for auto registering context commands -> do after release
// todo: store mail inbox in the database
// todo: document every command in each language. Do this by creating a md file for every language and update it with the info gathered from the commands.
// todo: table for users -> send dm responses in the user's preferred language

private val currentTime: LocalDateTime
    get() = LocalDateTime.now()

/**
 * Function that wakes up Michi
 * @author Slz
 */
suspend fun main() {
    println(currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm.SSS")))
    when(currentTime.hour) {
        in 0  until 6 -> println("Relaxing in the cozy dawn, aren't you?")
        in 6  until 12 -> println("Good morning! Want some coffee?")
        in 12 until 18 -> println("Good afternoon! Don't forget to touch some grass.")
        in 18 until 24 -> println("Good evening/night! Are you listening to Night Running?")
    }

    println(
        """
             __  __ ___ ___ _  _ ___ 
            |  \/  |_ _/ __| || |_ _|
            | |\/| || | (__| __ || | 
            |_|  |_|___\___|_||_|___|
            
        """.trimIndent()
    )

    // Initializing PostgresSQL
    DataBaseFactory.init()

    // Instantiating michi
    Michi()
}

/**
 * Main class, initialize Michi
 * @author Slz
 */
class Michi {

    companion object {
        val commandList = mutableListOf<MichiCommand>()
        val logger = LoggerFactory.getLogger(Michi::class.java)
    }

    init {

        // ideas from:
        // https://github.com/MrGaabriel/Ayla/blob/master/src/main/kotlin/com/github/mrgaabriel/ayla/AylaLauncher.kt <3
        val configFile = File(".env")

        if (!configFile.exists()) {
            configFile.createNewFile()
            logger.warn("Looks like you are trying to boot Michi for the first time.\n You must configure her in the file \".env\"\nFollow the example in the file \"example.env\"")
            exitProcess(Status.INFO)
        }

        logger.info("Configuration file loaded")
        val token = config["TOKEN"]

        // Initializing connection with Discord
        DefaultShardManagerBuilder.createDefault(token).apply {

            // Activity
            if (currentTime.hour >= 18 || currentTime.hour < 6) setActivity(Activity.listening("Night Running"))
            else setActivity(Activity.listening("Ready to"))

            // Event listeners
            addEventListeners(
                MessageListener,
                OnGuildReadyListener,
                CommandListener,
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
            enableCache(CacheFlag.VOICE_STATE)

        }.build()

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
        logger.info("Commands loaded | Amount: ${commandList.size}")
    }

    private fun searchCommandDirectories(initialDir: File): Set<File> {
        if (!initialDir.isDirectory || !initialDir.exists()) return emptySet()

        val commandDirectoriesList = mutableSetOf<File>()

        val visited = mutableSetOf(initialDir)
        val deque = LinkedBlockingDeque<File>().apply { add(initialDir) }

        while (deque.isNotEmpty()) {
            val dir = deque.pop()
            dir.listFiles()!!.forEach {
                if (!it.isDirectory || !it.exists() || it in visited) return@forEach
                commandDirectoriesList.add(it)
                visited.add(it)
                deque.add(it)
            }
        }

        return commandDirectoriesList
    }

}