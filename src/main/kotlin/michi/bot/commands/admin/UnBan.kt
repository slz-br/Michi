package michi.bot.commands.admin

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.awt.Color

/**
 * Object for the unban command, a command that unbans a user
 * if possible(the user to be unbanned is actually banned and if the sender
 * of the command has permission to unban the user)
 * @author Slz
 */
@Suppress("Unused")
object UnBan: MichiCommand("unban", GUILD_SCOPE) {
    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Unbans a user",
            DiscordLocale.ENGLISH_UK to "Unbans a user",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Desbane um usuário"
        )
    override val userPermissions: List<Permission>
        get() = listOf(
            Permission.ADMINISTRATOR,
            Permission.BAN_MEMBERS
        )

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.BAN_MEMBERS,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/$name <1st user>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument(
                name = "user",
                descriptionLocalization = mapOf(
                    DiscordLocale.ENGLISH_US to "The user to unban",
                    DiscordLocale.ENGLISH_UK to "The user to unban",
                    DiscordLocale.PORTUGUESE_BRAZILIAN to "O usuário para desbanir"
                ),
                type = OptionType.USER
            )
        )

    /**
     * Unbans the mentioned user(s) if possible.
     * @param context The interaction to retrieve info from.
     * @author Slz
     */
    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val guild = context.guild!!

        // guard clauses
        if (!canHandle(context)) return

        val subject = context.getOption("user")!!.asUser

        // if everything is right
        guild.unban(subject).queue()

        val success: YamlMap = getYML(context).yamlMap["success_messages"]!!
        val adminSuccess: YamlMap = success["admin"]!!
        val unbanMessage = adminSuccess.getText("unban_applied").split("\n")
        val warn: YamlMap = getYML(context).yamlMap["warn_messages"]!!
        val adminWarn: YamlMap = warn["admin"]!!

        EmbedBuilder().apply {
            setColor(Color.BLUE).setTitle(String.format(unbanMessage[0], Emoji.michiHappy))
            addField(String.format(unbanMessage[1], subject.asMention), "", false)
            setFooter(adminWarn.getText("unban_advice"))

        }.build()
            .let(context::replyEmbeds)
            .queue()
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val subject = context.getOption("user")?.asUser ?: return false
        val guild = context.guild!!
        val bot = guild.selfMember

        val err: YamlMap = getYML(sender.user).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!
        val adminErr: YamlMap = err["admin"]!!

        if (guild.getMemberById(subject.idLong) == null) {
            context.michiReply(String.format(adminErr.getText("user_not_banned"), subject.asMention, Emoji.michiHuh))
            return false
        }

        if (!sender.permissions.any(userPermissions::contains)) {
            context.michiReply(String.format(genericErr.getText("user_missing_perms"), Emoji.michiBlep))
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        return true
    }

}