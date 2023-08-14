package michi.bot.database

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import michi.bot.config
import michi.bot.database.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DataBaseFactory {

    suspend fun init() = withContext(IO) {
        Database.connect(config["DB_URL"], config["DB_DRIVER"], config["DB_USER"], config["DB_PASSWORD"])

        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.createMissingTablesAndColumns(
                GuildTable,
                BlacklistTable,
                UserTable,
                TypeRacerTable
            )
        }

    }

    suspend fun <T> query(block: () -> T): T = withContext(IO) { block() }

}