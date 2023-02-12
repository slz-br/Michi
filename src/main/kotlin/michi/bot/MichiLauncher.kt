package michi.bot

import io.github.cdimascio.dotenv.Dotenv
import michi.bot.listeners.*
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder

val config: Dotenv = Dotenv.configure().load()

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
            OnReadyListener(),
            SlashCommandListener()
        )

        // gateway intents
        .enableIntents(
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_VOICE_STATES
        )

        // build
        .build()
    }
}