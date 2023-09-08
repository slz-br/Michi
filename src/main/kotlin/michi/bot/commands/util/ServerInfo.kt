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
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.awt.Color

@Suppress("Unused")
object ServerInfo: MichiCommand("server-info", GUILD_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Gives you info about the server",
            DiscordLocale.ENGLISH_UK to "Gives you info about the server",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Dá informações sobre o servidor"
        )

    override val botPermissions: List<Permission>
        get() = listOf(
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_SEND_IN_THREADS
        )

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        val guild = context.guild!!

        val timeCreated = guild.timeCreated

        val success: YamlMap = getYML(context.user).yamlMap["success_messages"]!!
        val utilSuccess: YamlMap = success["util"]!!
        val guildInfoMessage = utilSuccess.getText("guild_info").split('\n')

        val embed = EmbedBuilder().apply {
            setColor(Color.WHITE)
            setTitle(guild.name)
            guild.description?.let {
                setDescription("${guildInfoMessage[0]}\n$it")
            }
            addField(guildInfoMessage[1], "<@${guild.ownerId}>", true)
            addField(guildInfoMessage[2], "${guild.memberCount}",true)
            addField(guildInfoMessage[3], guild.locale.languageName, true)
            addField(guildInfoMessage[4], "${timeCreated.year}/${timeCreated.monthValue}/${timeCreated.dayOfMonth}`(yyyy/mm/dd)`", false)
            setThumbnail(guild.iconUrl)
            setFooter(guild.name, guild.iconUrl)
        }

        context.michiReply(embed.build())
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild!!
        val bot = guild.selfMember

        val err: YamlMap = getYML(context.user).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err[("generic")]!!
        
        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        return true
    }
}