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
import java.time.format.DateTimeFormatter

@Suppress("Unused")
object UserInfo: MichiCommand("user-info", GLOBAL_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Sends you public info about a user",
            DiscordLocale.ENGLISH_UK to "Sends you public info about a user",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Dá informações públicas de um usuário"
        )

    override val usage: String
        get() = "/$name <user>"

    override val arguments = listOf(
        MichiArgument(
            "user",
            descriptionLocalization = mapOf(
                DiscordLocale.ENGLISH_US to "The user to get the info from",
                DiscordLocale.ENGLISH_UK to "The user to get the info from",
                DiscordLocale.PORTUGUESE_BRAZILIAN to "O usuário para pegar as informações"
            ),
            OptionType.USER
        )
    )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        val subjectUser = context.getOption("user")!!.asUser
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:SS")

        val success: YamlMap = getYML(context.user).yamlMap["success_messages"]!!
        val utilSuccess: YamlMap = success["util"]!!
        val userInfoMessage = utilSuccess.getText("user_info_message").split("\n")

        EmbedBuilder().apply {
            setTitle(String.format(userInfoMessage[0], subjectUser.globalName ?: subjectUser.effectiveName))
            addField(userInfoMessage[1], subjectUser.timeCreated.format(formatter), true)
            addBlankField(true)
            addField(userInfoMessage[2], "${subjectUser.globalName ?: subjectUser.effectiveName}`[id:${subjectUser.id}]`", true)
            if (context.guild != null) {
                val subjectMember = context.guild!!.getMemberById(subjectUser.idLong)

                if (subjectMember != null) {
                    addField(
                        userInfoMessage[3],
                        context.guild!!.getMemberById(subjectUser.idLong)!!.timeJoined.format(formatter),
                        true
                    )
                    addBlankField(true)
                    addField(userInfoMessage[4], if (subjectMember.isOwner) userInfoMessage[6] else userInfoMessage[7], true)
                }
            }
            addField(userInfoMessage[5], if (subjectUser.isBot) userInfoMessage[6] else userInfoMessage[7], true)
            setThumbnail(subjectUser.effectiveAvatarUrl)
        }.build().let(context::replyEmbeds).setEphemeral(true).queue()

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