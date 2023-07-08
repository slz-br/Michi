package michi.bot.database.dao

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import michi.bot.config
import michi.bot.database.DataBaseFactory
import michi.bot.database.rows.BlacklistRow
import michi.bot.database.tables.BlacklistTable

object BlacklistDAO {

    suspend fun post(guild: Guild, reason: String?) = withContext(Dispatchers.IO) {
        Database.connect(config["DB_URL"], config["DB_DRIVER"], config["DB_USER"], config["DB_PASSWORD"])

        if (find(guild)) return@withContext

        transaction {
            BlacklistRow.new {
                type = "guild"
                entityID = guild.idLong
                this.reason = reason
            }
            guild.leave().queue()

            commit()
        }
    }

    @Suppress("Unused")
    suspend fun post(user: User, reason: String?) = withContext(Dispatchers.IO) {
        Database.connect(config["DB_URL"], config["DB_DRIVER"], config["DB_USER"], config["DB_PASSWORD"])

        transaction {
            if (BlacklistRow.find { BlacklistTable.entityID eq user.idLong }.any()) return@transaction

            BlacklistRow.new {
                type = "user"
                entityID = user.idLong
                this.reason = reason
            }
            commit()
        }

    }

    suspend fun remove(user: User) = withContext(Dispatchers.IO) {
        Database.connect(config["DB_URL"], config["DB_DRIVER"], config["DB_USER"], config["DB_PASSWORD"])

        transaction {
            BlacklistRow.find { BlacklistTable.entityID eq user.idLong }.singleOrNull()?.let(BlacklistRow::delete)
            commit()
        }

    }

    suspend fun remove(guild: Guild) = withContext(Dispatchers.IO) {
        Database.connect(config["DB_URL"], config["DB_DRIVER"], config["DB_USER"], config["DB_PASSWORD"])

        transaction {
            BlacklistRow.find { BlacklistTable.entityID eq guild.idLong }.singleOrNull()?.let(BlacklistRow::delete)
            commit()
        }

    }

    suspend fun find(user: User): Boolean = DataBaseFactory.query {
        transaction {
            BlacklistRow.find { BlacklistTable.entityID eq user.idLong }.any()
        }
    }

    suspend fun find(guild: Guild?): Boolean = DataBaseFactory.query {
        guild ?: return@query false
        transaction {
            BlacklistRow.find { BlacklistTable.entityID eq guild.idLong }.any()
        }
    }

}