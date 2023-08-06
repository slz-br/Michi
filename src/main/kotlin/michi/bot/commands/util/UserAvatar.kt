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
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.awt.Color

@Suppress("Unused")
object UserAvatar: MichiCommand("user-avatar", GLOBAL_SCOPE) {

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/$name <user>"

    override val arguments: List<MichiArgument>
        get() = listOf(
            MichiArgument("user", OptionType.USER)
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return

        val user = context.getOption("user")?.asUser ?: return

        val embed = EmbedBuilder().apply {
            setColor(Color.WHITE)
            setDescription("${user.asMention}'s avatar")
            setImage(user.avatarUrl)
        }
        context.michiReply(embed.build())
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild

        guild?.let {
            val bot = it.selfMember

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