package michi.bot.commands.admin

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.database.dao.GuildsDAO
import michi.bot.listeners.CommandAutoCompletionListener.languageAutoCompletion
import michi.bot.util.Emoji
import michi.bot.util.Language
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import michi.bot.util.updateGuildCommands
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

@Suppress("unused")
object SetLanguage: MichiCommand("language", GUILD_SCOPE) {

    override val ownerOnly = true

    override val usage = "/$name <language-name>"

    override val arguments = listOf(MichiArgument("language-name", OptionType.STRING, hasAutoCompletion = true))

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return

        val guild = context.guild!!
        val langName = context.getOption("language-name")!!.asString

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!

        if (langName !in languageAutoCompletion) {
            context.michiReply(String.format(genericErr.getText("option_err"), Emoji.smolMichiAngry))
            return
        }

        GuildsDAO.setLanguage(guild, Language.valueOfOrNull(langName) ?: Language.EN_US)
        updateGuildCommands(guild)
        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val adminSuccess: YamlMap = success["admin"]!!
        context.michiReply(adminSuccess.getText("language_changed"))
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val guild = context.guild!!
        val langName = context.getOption("language-name")!!.asString
        val currentLang = GuildsDAO.getLanguage(guild)
        val desiredLang = Language.valueOfOrNull(langName)

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val adminErr: YamlMap = err["admin"]!!

        if (!sender.isOwner && ownerOnly) {
            context.michiReply(genericErr.getText("user_isnt_owner"))
            return false
        }

        if (desiredLang == null) {
            context.michiReply(String.format(genericErr.getText("option_err"), Emoji.smolMichiAngry))
            return false
        }

        if (currentLang == desiredLang) {
            context.michiReply(adminErr.getText("same_language"))
            return false
        }

        return true
    }

}