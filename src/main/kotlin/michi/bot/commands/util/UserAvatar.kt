package michi.bot.commands.util

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GLOBAL_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.awt.Color

@Suppress("Unused")
object UserAvatar: MichiCommand("user-avatar", GLOBAL_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Sends you the profile picture of a user",
            DiscordLocale.ENGLISH_UK to "Sends you the profile picture of a user",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Dá a imagem de perfil de um usuário"
        )

    override val usage: String
        get() = "/$name <user>"

    override val arguments = listOf(
        MichiArgument(
            name = "user",
            descriptionLocalization = mapOf(
                DiscordLocale.ENGLISH_US to "The user to get the profile picture from",
                DiscordLocale.ENGLISH_UK to "The user to get the profile picture from",
                DiscordLocale.PORTUGUESE_BRAZILIAN to "O usuário para pegar a foto de perfil"
            ),
            type = OptionType.USER,
            isRequired = false
        )
    )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return

        val user = context.getOption("user")?.asUser ?: context.user

        val success: YamlMap = getYML(context.user).yamlMap["success_messages"]!!
        val utilSuccess: YamlMap = success["util"]!!

        val embed = EmbedBuilder().apply {
            setColor(Color.WHITE)
            setDescription(String.format(utilSuccess.getText("user_avatar"), user.asMention))
            setImage(user.avatarUrl)
        }
        context.michiReply(embed.build())
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild

        guild?.let {
            val bot = it.selfMember

            val err: YamlMap = getYML(context.user).yamlMap["error_messages"]!!
            val genericErr: YamlMap = err["generic"]!!
            
            if (!bot.permissions.containsAll(botPermissions)) {
                context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
                return false
            }

        }

        return true
    }

}