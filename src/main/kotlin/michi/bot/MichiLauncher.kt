package michi.bot

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI
import io.github.cdimascio.dotenv.Dotenv
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import org.slf4j.*
import java.io.File
import java.util.concurrent.LinkedBlockingDeque
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import michi.bot.commands.CommandDeactivated
import michi.bot.listeners.*
import michi.bot.database.DataBaseFactory
import michi.bot.commands.MichiCommand

/**
 * Holder of the environment variables.
 * please check the .env file or create it if you haven't yet.
 *
 * Usage: config[["KEY"]]
 *
 * Example: config[["TOKEN"]] // to retrieve the Token from the .env file.
 */
val config: Dotenv = Dotenv.configure().load()

private val currentTime: LocalDateTime
    get() = LocalDateTime.now()

val perspectiveAPI: PerspectiveAPI? = PerspectiveAPI.create(config["PERSPECTIVE_API_TOKEN"])

/**
 * Function that wakes up Michi
 * @author Slz
 */
suspend fun main() {
    println(currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm.SSS")))
    when(currentTime.hour) {
        in 0 ..< 6 -> println("Relaxing in the cozy dawn, aren't you?")
        in 6 ..< 12 -> println("Good morning! Do you want some coffee?")
        in 12 ..< 18 -> println("Good afternoon! Don't forget to touch some grass.")
        in 18 ..< 24 -> println("Good evening/night! Are you listening to Night Running?")
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

        /**
         * List containing all Michi command's. The commands are added at run time to the commandList via reflection.
         * All classes that extend [michi.bot.commands.MichiCommand] except the ones annotated with [michi.bot.commands.CommandDeactivated]
         * @see registerCommands
         */
        val commandList = mutableListOf<MichiCommand>()

        /**
         * Logger for the Michi class. Used to print information about the config files and if the commands were properly
         * registered.
         */
        val logger: Logger = LoggerFactory.getLogger(Michi::class.java)

        /**
         * Function used to register Michi's commands. It looks up for all the classes within the subdirectories of [michi.bot.commands]
         * that extend [michi.bot.commands.MichiCommand] and aren't annotated with [michi.bot.commands.CommandDeactivated].
         * @see searchCommandDirectories
         */

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

                    val commandObjectInstance = Class.forName(qualifiedName).kotlin.objectInstance

                    if (
                        commandObjectInstance !is MichiCommand
                        || commandObjectInstance::class.annotations.contains(CommandDeactivated())
                    ) return@fileLoop

                    commandList += commandObjectInstance
                }
            }
            logger.info("Commands loaded | Amount: ${commandList.size}")
        }

        /**
         * Function that uses the breadth first search algorithm to look up for all the directories within
         * the [michi.bot.commands] directory.
         */
        private fun searchCommandDirectories(initialDir: File): Set<File> {
            if (!initialDir.isDirectory || !initialDir.exists()) return emptySet()

            val commandDirectoriesList = mutableSetOf<File>()

            val visited = mutableSetOf(initialDir)
            val deque = LinkedBlockingDeque<File>().apply { add(initialDir) }

            while (deque.isNotEmpty()) {
                val dir = deque.pop()
                dir.listFiles()!!.forEach {
                    if (!it.isDirectory || !it.exists() || it in visited) return@forEach
                    commandDirectoriesList += it
                    visited += it
                    deque += it
                }
            }

            return commandDirectoriesList
        }
    }

    init {

        logger.info("Configuration file loaded")
        val token = config["TOKEN"]

        if (perspectiveAPI == null) {
            logger.warn("The perspective api key is null or invalid. This means that the mail commands won't work.\nFor more details read the .env.example file.")
        }

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
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MEMBERS
            )

            setMemberCachePolicy(MemberCachePolicy.ALL)

            // Enable cache
            enableCache(CacheFlag.VOICE_STATE)

        }.build()

        registerCommands()
    }

}