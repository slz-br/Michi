package michi.bot.listeners

import michi.bot.commands.mail.Mail
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object ModalInteractionListener: ListenerAdapter() {

    override fun onModalInteraction(event: ModalInteractionEvent) {

        when(event.modalId) {
            "mail" -> Mail.sendMail(event)
        }


    }
}