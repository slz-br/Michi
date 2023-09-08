package michi.bot.commands.util

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.awt.Color

@Suppress("Unused")
object ServerIcon: MichiCommand("server-icon", GUILD_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Gives you the image of the server's icon",
            DiscordLocale.ENGLISH_UK to "Gives you the image of the server's icon",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Dá a imagem do icone do servidor"
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        val guild = context.guild!!

        val success: YamlMap = getYML(context.user).yamlMap["success_messages"]!!
        val utilSuccess: YamlMap = success["util"]!!

        EmbedBuilder().apply {
            setColor(Color.WHITE)
            setImage(guild.iconUrl)
            setDescription(String.format(utilSuccess.getText("guild_icon"), guild.name))
        }.build().let { context.michiReply(it) }
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild ?: return false
        val bot = guild.selfMember

        val err: YamlMap = getYML(context.user).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!

        if (guild.icon == null) {
            context.michiReply("This server doesn't have an icon ${Emoji.michiShrug}")
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        return true
    }

}