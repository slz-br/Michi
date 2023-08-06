package michi.bot.listeners

import michi.bot.util.updateCommands
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Object that holds the event handler [onReady].
 * @author Slz
 */
object OnReadyListener: ListenerAdapter() {

    /**
     * Called whenever the bot loads.
     * @author Slz
     */
    override fun onReady(event: ReadyEvent) = updateCommands(event)

}