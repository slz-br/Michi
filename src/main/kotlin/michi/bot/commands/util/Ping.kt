package michi.bot.commands.util

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import michi.bot.commands.CommandScope.GLOBAL_SCOPE
import michi.bot.commands.MichiCommand
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Suppress("Unused")
object Ping: MichiCommand("ping", GLOBAL_SCOPE) {

    override val usage: String
        get() = "/ping"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        if (!canHandle(context)) return
        context.michiReply("${Emoji.michiSmug} | pong!\nAPI ping: ${context.jda.gatewayPing} ms")
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val guild = context.guild

        guild?.let {
            val bot = guild.selfMember

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