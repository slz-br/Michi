package michi.bot.commands

/**
 * Used to define the scope of the MichiCommand.
 *
 * [GLOBAL_SCOPE] means that the command can be executed in guilds and direct messages.<br>
 * [GUILD_SCOPE] means that the command can only be used in guilds.
 * @sample
 * @author Slz
 */
enum class CommandScope {
    GLOBAL_SCOPE,
    GUILD_SCOPE
}