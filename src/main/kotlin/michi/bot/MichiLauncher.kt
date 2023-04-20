package michi.bot

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import michi.bot.commands.*
import michi.bot.commands.admin.Ban
import michi.bot.commands.admin.Clear
import michi.bot.commands.admin.SlowMode
import michi.bot.commands.admin.UnBan
import michi.bot.commands.math.Math
import michi.bot.commands.misc.*
import michi.bot.listeners.*
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import okhttp3.internal.http2.Http2Connection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val config: Dotenv = Dotenv.configure().load()
val logger: Logger = LoggerFactory.getLogger(Http2Connection.Listener::class.java)
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
        val commandList = mutableListOf<MichiCommand>()
    }
    init {

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

        // Initializing JDA
        val michi = DefaultShardManagerBuilder.createDefault(token)

        // Activity
        michi.setActivity(Activity.watching("Brand New Animal!"))

        // Event listeners
        .addEventListeners(
            MessageListener(),
            OnGuildReadyListener(),
            SlashCommandListener(),
            ModalInteractionListener(),
            ButtonListener()
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
