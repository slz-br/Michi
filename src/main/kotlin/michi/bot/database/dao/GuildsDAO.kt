package michi.bot.database.dao

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import michi.bot.config
import michi.bot.database.DataBaseFactory
import michi.bot.database.rows.GuildRow
import michi.bot.database.tables.GuildTable
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.interactions.DiscordLocale
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object GuildsDAO {

    /**
     * Adds a guild to the database.
     * @author Slz
     */
    suspend fun post(guild: Guild): Boolean = DataBaseFactory.query {
        Database.connect(config["DB_URL"], config["DB_DRIVER"], config["DB_USER"], config["DB_PASSWORD"])

        transaction {
            if (!GuildRow.find { GuildTable.guildID eq guild.idLong }.empty()) return@transaction false

            GuildRow.new {
                guildID = guild.idLong
                name = guild.name
                ownerID = guild.ownerIdLong
                iconURL = guild.iconUrl
                logsChannelID = null
                language = when (guild.locale) {
                    DiscordLocale.PORTUGUESE_BRAZILIAN -> "pt-br"
                    else -> "en-us"
                }
                djs = ""
                musicQueue = ""
            }
            return@transaction true
        }

    }

    /**
     * Deletes a guild from the database.
     * @author Slz
     */
    suspend fun delete(guild: Guild) = DataBaseFactory.query {
        Database.connect(config["DB_URL"], config["DB_DRIVER"], config["DB_USER"], config["DB_PASSWORD"])

        transaction {
            GuildRow.find { GuildTable.guildID eq guild.idLong }.singleOrNull()?.let(GuildRow::delete)
            commit()
        }

    }

    suspend fun get(guild: Guild?) = withContext(Dispatchers.IO) {
        transaction {
            guild ?: return@transaction null
            GuildRow.find { GuildTable.guildID eq guild.idLong }.singleOrNull()
        }
    }

    suspend fun getLogsChannel(guild: Guild) = get(guild)?.logsChannelID

    suspend fun setLogChannel(guild: Guild, channel: GuildMessageChannel?) = withContext(Dispatchers.IO) {
        val guildRow = get(guild)
        transaction {
            guildRow?.logsChannelID = channel?.idLong
            commit()
        }
    }

    suspend fun setMusicQueue(guild: Guild, queue: String?) {
        val guildRow = get(guild)
        transaction {
            guildRow?.musicQueue = queue
            commit()
        }
    }

    suspend fun selectMusicQueue(guild: Guild): String? = get(guild)?.musicQueue

    suspend fun setOwner(guild: Guild, newOwner: Member) {
        val guildRow = get(guild)
        transaction {
            guildRow?.ownerID = newOwner.idLong
            commit()
        }
    }

}