package michi.bot.commands.util

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GLOBAL_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.database.dao.GuildDao
import michi.bot.database.dao.UserDao
import michi.bot.util.Emoji
import michi.bot.util.Language
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import michi.bot.util.updateGuildCommands
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("unused")
object SetLanguage: MichiCommand("language", GLOBAL_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Changes the language of the bot responses in the server",
            DiscordLocale.ENGLISH_UK to "Changes the language of the bot responses",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Muda a linguagem das respostas do bot"
        )

    override val ownerOnly = true

    override val usage = "/$name <language-name>"

    override val arguments = listOf(
        MichiArgument(
            name = "language-name",
            descriptionLocalization = mapOf(
                DiscordLocale.ENGLISH_US to "Changes the language of the bot responses",
                DiscordLocale.ENGLISH_UK to "Changes the language of the bot responses",
                DiscordLocale.PORTUGUESE_BRAZILIAN to "Muda a linguagem das respostas do bot"
            ),
            type = OptionType.STRING,
            hasAutoCompletion = true
        )
    )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return

        val langName = context.getOption("language-name")!!.asString

        context.guild?.let { guild ->
            GuildDao.setLanguage(guild, Language.valueOfOrNull(langName) ?: Language.EN_US)
            updateGuildCommands(guild)
            val success: YamlMap = getYML(guild).yamlMap["success_messages"]!!
            val adminSuccess: YamlMap = success["admin"]!!
            context.michiReply(adminSuccess.getText("guild_language_changed"))
            return
        }

        val sender = context.user

        UserDao.postIfAbsent(sender).let {
            transaction {
                it.preferredLanguage = langName
            }
        }

        val success: YamlMap = getYML(sender).yamlMap["success_messages"]!!
        val adminSuccess: YamlMap = success["admin"]!!
        context.michiReply(adminSuccess.getText("language_changed"))
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.user
        val guild = context.guild
        val langName = context.getOption("language-name")!!.asString
        val desiredLang = Language.valueOfOrNull(langName)

        val err: YamlMap = getYML(sender).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!

        if (desiredLang == null) {
            context.michiReply(String.format(genericErr.getText("option_err"), Emoji.smolMichiAngry))
            return false
        }

        guild?.let {
            val senderAsMember = context.member!!
            val currentLang = GuildDao.getLanguage(guild)
            val errInGuildLanguage: YamlMap = getYML(guild).yamlMap["error_messages"]!!
            val adminErrInGuildLanguage: YamlMap = errInGuildLanguage["admin"]!!

            if (!senderAsMember.isOwner && ownerOnly) {
                context.michiReply(genericErr.getText("user_isnt_owner"))
                return false
            }

            if (currentLang == desiredLang) {
                context.michiReply(adminErrInGuildLanguage.getText("guild_same_language"))
                return false
            }

            return true
        }

        val currentLang = UserDao.postIfAbsent(sender).preferredLanguage
        val adminErr: YamlMap = err["admin"]!!

        if (langName == currentLang) {
            context.michiReply(adminErr.getText("same_language"))
            return false
        }

        return true
    }

}