package michi.bot

import io.github.cdimascio.dotenv.Dotenv
import michi.bot.listeners.*
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag
import okhttp3.internal.http2.Http2Connection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val config: Dotenv = Dotenv.configure().load()
val logger: Logger = LoggerFactory.getLogger(Http2Connection.Listener::class.java)

/**
 * Function that wakes up Michi
 * @author Slz
 */

fun main() {
   Michi()
}

class Michi {

    init {

        val token = config.get("TOKEN")
        val michi = DefaultShardManagerBuilder.createDefault(token)

        // activity
        michi.setActivity(Activity.watching("Brand New Animal"))

        // event listeners
        .addEventListeners(
            MessageListener(),
            OnGuildReadyListener(),
            SlashCommandListener()
        )

        // gateway intents
        .enableIntents(
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_VOICE_STATES
        )

        // enable cache
        .enableCache(
            CacheFlag.VOICE_STATE
        )

        // build
        .build()

        // logger message
        logger.info("Michi is ready!")
    }
}