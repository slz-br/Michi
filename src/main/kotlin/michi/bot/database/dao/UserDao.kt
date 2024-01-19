package michi.bot.database.dao

import michi.bot.database.DataBaseFactory
import michi.bot.database.rows.UserRow
import michi.bot.database.tables.UserTable
import michi.bot.util.Language
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

object UserDao {

    suspend fun remove(user: User) {
        get(user) ?: return
        transaction {
            UserTable.deleteWhere { userId eq user.idLong }
        }
    }

    suspend fun get(user: User) = DataBaseFactory.query {
        transaction {
            UserRow.find { UserTable.userId eq user.idLong }.firstOrNull()
        }
    }

    suspend fun postIfAbsent(user: User): UserRow {
        return get(user) ?: transaction {
            UserRow.new {
                userId = user.idLong
                preferredLanguage = Language.EN_US.value
            }
        }
    }

}