package michi.bot.database

import kotlinx.coroutines.*
import michi.bot.config
import michi.bot.database.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DataBaseFactory {

    suspend fun init() = withContext(Dispatchers.IO) {
        Database.connect(config["DB_URL"], config["DB_DRIVER"], config["DB_USER"], config["DB_PASSWORD"])

        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.createMissingTablesAndColumns(
                GuildTable,
                BlacklistTable
            )

            commit()
        }

    }

    suspend fun <T> query(block: () -> T): T = withContext(Dispatchers.IO) { block() }

}