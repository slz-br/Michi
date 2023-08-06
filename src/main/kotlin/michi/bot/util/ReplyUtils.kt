package michi.bot.util

import com.charleskorn.kaml.*

import michi.bot.database.dao.GuildsDAO
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.InteractionHook

/**
 * Object containing useful functions for replying to events.
 * It also provides functions for multilingual replies.
 */
object ReplyUtils {

    /**
     * Gets the [YamlNode] of the Guild's language. For example: If the owner set that the preferred for the bot
     * is Portuguese, this function will get this preferred language, look for the "pt-br.yml" file and return a
     * [YamlNode] out of its content.
     * @param context The [SlashCommandInteractionEvent]
     * @return [YamlNode]
     * @author Slz
     */
    suspend fun getYML(context: SlashCommandInteractionEvent): YamlNode {
        val guild = context.guild ?: return Yaml.default.parseToYamlNode(javaClass.classLoader.getResource("langs/en-us.yml")!!.readText())

        val lang = GuildsDAO.getLanguage(guild).value

        val yamlAsString = javaClass.classLoader.getResource("langs/$lang.yml")!!.readText()

        return Yaml.default.parseToYamlNode(yamlAsString)
    }

    fun getYML(lang: Language): YamlNode =
       Yaml.default.parseToYamlNode(javaClass.classLoader.getResource("langs/${lang.value}.yml")!!.readText())


    /**
     * Gets the [YamlNode] of the Guild's language. For example: If the owner set that the preferred for the bot
     * is Portuguese, this function will get this preferred language, look for the "pt-br.yml" file and return a
     * [YamlNode] out of its content.
     * @param context The [MessageReceivedEvent]
     * @return [YamlNode]
     * @author Slz
     */
    suspend fun getYML(context: MessageReceivedEvent): YamlNode {
        if (!context.isFromGuild) return Yaml.default.parseToYamlNode(javaClass.classLoader.getResource("langs/en-us.yml")!!.readText())
        val guild = context.guild
        val lang = GuildsDAO.getLanguage(guild).value
        val yamlAsString = javaClass.classLoader.getResource("langs/$lang.yml")!!.readText()

        return Yaml.default.parseToYamlNode(yamlAsString)
    }

    /**
     * Gets the [YamlNode] of the Guild's language. For example: If the owner set that the preferred for the bot
     * is Portuguese, this function will get this preferred language, look for the "pt-br.yml" file and return a
     * [YamlNode] out of its content.
     * @param context The [ButtonInteractionEvent]
     * @return [YamlNode]
     * @author Slz
     */
    suspend fun getYML(context: ButtonInteractionEvent): YamlNode {
        val guild = context.guild ?: return Yaml.default.parseToYamlNode(javaClass.classLoader.getResource("langs/en-us.yml")!!.readText())
        val lang = GuildsDAO.getLanguage(guild).value
        val yamlAsString = javaClass.classLoader.getResource("langs/$lang.yml")!!.readText()

        return Yaml.default.parseToYamlNode(yamlAsString)
    }

    /**
     * Gets the [YamlNode] of the Guild's language. For example: If the owner set that the preferred for the bot
     * is Portuguese, this function will get this preferred language, look for the "pt-br.yml" file and return a
     * [YamlNode] out of its content.
     * @param guild The [Guild]
     * @return [YamlNode]
     * @author Slz
     */
    suspend fun getYML(guild: Guild): YamlNode {
        val lang = GuildsDAO.getLanguage(guild).value
        val yamlAsString = javaClass.classLoader.getResource("langs/$lang.yml")!!.readText()

        return Yaml.default.parseToYamlNode(yamlAsString)
    }

    /**
     * Get the content of a key in the YamlMap as a String.
     * @return The String content of the key or "404 - NOT FOUND" if the key doesn't exist.
     * @author Slz
     */
    fun YamlMap.getText(key: String) = getScalar(key)?.content ?: "404 - NOT FOUND"


    /**
     * Convenience function for replying to a [SlashCommandInteractionEvent] with a simple text message.
     * If your handling of the interaction can take more than 3 seconds, you should use JDA's deferReply function and [michiSendMessage] instead.
     * You must use JDA's SlashCommandInteractionEvent.reply() function if you want to perform more complex
     * operations with your reply, such as: Adding buttons, components, actionRows etc.
     * You may get these error responses:
     *
     * - UNKNOWN_INTERACTION
     * If the interaction has already been acknowledged or timed out
     * - MESSAGE_BLOCKED_BY_AUTOMOD
     *
     * If this message was blocked by an [net.dv8tion.jda.api.entities.automod.AutoModRule]
     * - MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER
     *
     * If this message was blocked by the harmful link filter
     *
     * @param message The text message to reply. Default = ""
     * @param isEphemeral Whether the message should be visible to other people. Default = true
     *
     * @throws IllegalArgumentException If null is provided or the string is longer than 2000 characters
     *
     * @author Slz
     *
     * @see michiSendMessage
     */
    @SuppressWarnings
    fun SlashCommandInteractionEvent.michiReply(message: String, isEphemeral: Boolean = true) {
        reply(message).setEphemeral(isEphemeral).queue()
    }

    /**
     * Convenience function for replying to a [SlashCommandInteractionEvent] with embeds. Additionally, you can add a text message too.
     * If your handling of the interaction can take more than 3 seconds, you should use
     * JDA's deferReply function and [michiSendMessage] instead.
     * You must use JDA's SlashCommandInteractionEvent.replyEmbeds() function if you want to perform more complex
     * operations with your reply, such as: Adding buttons, components, actionRows etc.
     * You may get these error responses:
     *
     * - UNKNOWN_INTERACTION
     * If the interaction has already been acknowledged or timed out
     *
     * - MESSAGE_BLOCKED_BY_AUTOMOD
     * If this message was blocked by an [net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     * - MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER
     * If this message was blocked by the harmful link filter
     *
     * @param message The text message to reply. Default = ""
     * @param embed The embeds to add to the message
     * @param isEphemeral Whether the message should be visible to other people. Default = true
     *
     * @throws IllegalArgumentException If you provide null to the message or the message content is longer than 2000 characters
     * @throws IllegalArgumentException If you provide null to the embeds or if you provided more than 10 embeds
     *
     * @author Slz
     *
     * @see michiSendMessage
     */
    @SuppressWarnings
    fun SlashCommandInteractionEvent.michiReply(vararg embed: MessageEmbed, isEphemeral: Boolean = true, message: String = "") {
        michiReply(embed.asList(), isEphemeral, message)
    }

    /**
     * Convenience function for replying to a [SlashCommandInteractionEvent] with embeds. Additionally, you can add a text message too.
     * If your handling of the interaction can take more than 3 seconds, you should use
     * JDA's deferReply function and [michiSendMessage] instead.
     * You must use JDA's SlashCommandInteractionEvent.replyEmbeds() function if you want to perform more complex
     * operations with your reply, such as: Adding buttons, components, actionRows etc.
     * You may get these error responses:
     *
     * - UNKNOWN_INTERACTION
     * If the interaction has already been acknowledged or timed out
     *
     * - MESSAGE_BLOCKED_BY_AUTOMOD
     * If this message was blocked by an [net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     * - MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER
     * If this message was blocked by the harmful link filter
     *
     * @param message The text message to reply. Default = ""
     * @param embeds The embeds to add to the message
     * @param isEphemeral Whether the message should be visible to other people. Default = true
     *
     * @throws IllegalArgumentException If you provide null to the message or the message content is longer than 2000 characters
     * @throws IllegalArgumentException If you provide null to the embeds or if you provided more than 10 embeds
     *
     * @author Slz
     *
     * @see michiSendMessage
     */
    @SuppressWarnings
    fun SlashCommandInteractionEvent.michiReply(embeds: Collection<MessageEmbed>, isEphemeral: Boolean = true, message: String = "") {
        reply(message).addEmbeds(embeds).setEphemeral(isEphemeral).queue()
    }

    /**
     * Convenience function for replying to a [SlashCommandInteractionEvent] that was already acknowledge with [net.dv8tion.jda.api.interactions.callbacks.IReplyCallback.deferReply] with a simple text message.
     * You should use it with deferReply before. If you know that your handling of the interaction won't take longer than 3 seconds you shouldn't defer the reply and shouldn't use this function, instead, you should use [michiReply].
     * You must use JDA's InteractionHook.sendMessage() function if you want to perform more complex
     * operations with your reply, such as: Adding buttons, components, actionRows etc.
     * You may get these error responses:
     *
     * - UNKNOWN_WEBHOOK
     * The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *
     * - MESSAGE_BLOCKED_BY_AUTOMOD
     * If this message was blocked by an [net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     * - MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER
     * If this message was blocked by the harmful link filter
     *
     * @param message The text message to reply. Default = ""
     * @param isEphemeral Whether the message should be visible to other people. Default = true
     *
     * @throws IllegalArgumentException If you provide null to the message or the message content is longer than 2000 characters
     *
     * @author Slz
     *
     * @see michiReply
     */
    @SuppressWarnings
    fun InteractionHook.michiSendMessage(message: String = "", isEphemeral: Boolean = true) {
        sendMessage(message).setEphemeral(isEphemeral).queue()
    }

    /**
     * Convenience function for replying to a [SlashCommandInteractionEvent] that was already acknowledge with
     * JDA's deferReply function. You should use it with deferReply before.
     * If you know that your handling of the interaction won't take longer than 3 seconds you shouldn't defer the reply
     * and shouldn't use this function, instead, you should use [michiReply].
     * You must use JDA's InteractionHook.sendMessageEmbeds() if you want to perform more complex
     * operations with your reply, such as: Adding buttons, components, actionRows etc.
     * You may get these error responses:
     *
     * - UNKNOWN_WEBHOOK
     * The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *
     * - MESSAGE_BLOCKED_BY_AUTOMOD
     * If this message was blocked by an [net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     * - MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER
     * If this message was blocked by the harmful link filter
     *
     * @param embed The embeds to add to the message
     * @param isEphemeral Whether the message should be visible to other people. This should be the same as the value
     * passed to the deferReply() function(false if no value was passed). Default = true
     * @param message The text message to reply. Default = ""

     * @throws IllegalArgumentException If you provide null to the message or the message content is longer than 2000 characters.
     * @throws IllegalArgumentException If you provide null to the embeds or if you provided more than 10 embeds.
     *
     * @author Slz
     *
     * @see michiReply
     */
    fun InteractionHook.michiSendMessage(vararg embed:MessageEmbed, isEphemeral: Boolean = true, message: String = "") {
        michiSendMessage(embed.asList(), isEphemeral, message)
    }

    /**
     * Convenience function for replying to a [SlashCommandInteractionEvent] that was already acknowledge with
     * JDA's deferReply function. You should use it with deferReply before.
     * If you know that your handling of the interaction won't take longer than 3 seconds you shouldn't defer the reply
     * and shouldn't use this function, instead, you should use [michiReply].
     * You must use JDA's InteractionHook.sendMessageEmbeds() if you want to perform more complex
     * operations with your reply, such as: Adding buttons, components, actionRows etc.
     * You may get these error responses:
     *
     * - UNKNOWN_WEBHOOK
     * The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *
     * - MESSAGE_BLOCKED_BY_AUTOMOD
     * If this message was blocked by an [net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     * - MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER
     * If this message was blocked by the harmful link filter
     *
     * @param embeds The embeds to add to the message
     * @param isEphemeral Whether the message should be visible to other people. This should be the same as the value
     * passed to the deferReply() function(false if no value was passed). Default = true
     * @param message The text message to reply. Default = ""

     * @throws IllegalArgumentException If you provide null to the message or the message content is longer than 2000 characters.
     * @throws IllegalArgumentException If you provide null to the embeds or if you provided more than 10 embeds.
     *
     * @author Slz
     *
     * @see michiReply
     */
    @Suppress("Unused")
    fun InteractionHook.michiSendMessage(embeds: Collection<MessageEmbed>, isEphemeral: Boolean = true, message: String = "") {
        sendMessage(message).addEmbeds(embeds).setEphemeral(isEphemeral).queue()
    }

}