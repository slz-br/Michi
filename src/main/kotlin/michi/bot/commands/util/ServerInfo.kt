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
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

@Suppress("Unused")
object ServerInfo: MichiCommand("server-info", GUILD_SCOPE) {

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override val usage: String
        get() = "/server-info"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val guild = context.guild ?: return

        if (!canHandle(context)) return

        val timeCreated = guild.timeCreated

        val embed = EmbedBuilder().apply {
            setColor(Color.WHITE)
            setTitle(guild.name)
            setDescription("Description:\n ${guild.description ?: "No description provided ${Emoji.michiShrug}"}")
            addField("Guild Owner:", "<@${guild.ownerId}>", false)
            addField("Members count:", "${guild.memberCount}",false)
            addField("Guild language:", guild.locale.languageName, false)
            addField("Creation date:", "${timeCreated.year}/${timeCreated.monthValue}/${timeCreated.dayOfMonth}`(yyyy/mm/dd format)`", false)
            setThumbnail(guild.iconUrl)
            setFooter(guild.name, guild.iconUrl)
        }

        context.michiReply(embed.build())
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild ?: return false
        val bot = guild.selfMember

        val err: YamlMap = getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err[("generic")]!!
        
        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        return true
    }
}