package michi.bot.commands.util

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GLOBAL_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.database.dao.GuildsDAO
import michi.bot.util.Emoji
import michi.bot.util.Language
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import java.awt.Color

@Suppress("Unused")
object Help: MichiCommand("help", GLOBAL_SCOPE) {

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        val sender = context.user
        val guild = context.guild

        val language = if (guild != null) GuildsDAO.getLanguage(guild) else Language.EN_US

        val success: YamlMap = getYML(language).yamlMap["success_messages"]!!
        val utilSuccess: YamlMap = success["util"]!!

        val helpMessage = utilSuccess.getText("help_message").split('\n')

       EmbedBuilder().apply {
           setColor(Color.MAGENTA)
           setTitle(String.format(helpMessage[0], sender.globalName ?: sender.effectiveName, Emoji.michiSmug))
           addField(
               helpMessage[1],
               helpMessage[2],
               false
           )
           addField(
               helpMessage[3],
               helpMessage[4],
               false
           )
       }.build().let { context.michiReply(it) }

    }

    suspend fun execute(context: UserContextInteractionEvent) {
        val sender = context.user
        val guild = context.guild

        val language = if (guild != null) GuildsDAO.getLanguage(guild) else Language.EN_US

        val success: YamlMap = getYML(language).yamlMap["success_messages"]!!
        val utilSuccess: YamlMap = success["util"]!!

        val helpMessage = utilSuccess.getText("help_message").split('\n')

        EmbedBuilder().apply {
            setColor(Color.MAGENTA)
            setTitle(
                String.format(
                    helpMessage[0],
                    sender.globalName ?: sender.effectiveName,
                    Emoji.michiSmug
                )
            )
            addField(
                helpMessage[1],
                helpMessage[2],
                false
            )
            addField(
                helpMessage[3],
                helpMessage[4],
                false
            )
        }.build().let { context.replyEmbeds(it) }.setEphemeral(true).queue()
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild

        guild?.let {
            val bot = guild.selfMember

            val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
            val genericErr: YamlMap = err["generic"]!!

            if (!bot.permissions.containsAll(botPermissions)) {
                context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
                return false
            }
        }

        return true
    }

}