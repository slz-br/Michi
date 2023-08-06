package michi.bot.commands.music.dj

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GUILD_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

val GuildDJMap = mutableMapOf<Guild, MutableSet<Member>>()

@Suppress("Unused")
object SetDJ: MichiCommand("set-dj", GUILD_SCOPE) {

    override val ownerOnly = true

    override val arguments = listOf(MichiArgument("user", OptionType.USER))

    override val usage: String
        get() = "/$name <user>"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        val guild = context.guild!!
        val member = context.getOption("user")!!.asMember!!
        val guildDjMap = GuildDJMap.computeIfAbsent(guild) { mutableSetOf() }

        val success: YamlMap = ReplyUtils.getYML(guild).yamlMap["success_messages"]!!
        val musicDJSuccess: YamlMap = success["music_dj"]!!

        if (member !in guildDjMap) {
            guildDjMap + member
            context.michiReply(String.format(musicDJSuccess.getText("dj_setted"), member.asMention))
        } else {
            guildDjMap - member
            context.michiReply(String.format(musicDJSuccess.getText("dj_removed"), member.asMention))
        }

    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val sender = context.member!!
        val guild = context.guild!!
        val member = context.getOption("user")?.asMember
        val bot = guild.selfMember

        val err: YamlMap = ReplyUtils.getYML(context).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!

        if (member == null) {
            context.michiReply(String.format(genericErr.getText("user_not_found"), Emoji.michiShrug))
            return false
        }

        if (!bot.permissions.containsAll(botPermissions)) {
            context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
            return false
        }

        if (!sender.isOwner && ownerOnly) {
            context.michiReply(genericErr.getText("user_isnt_owner"))
            return false
        }

        return true
    }

}