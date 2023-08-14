package michi.bot.database.dao

import michi.bot.database.DataBaseFactory
import michi.bot.database.rows.TypeRacerRow
import michi.bot.database.rows.UserRow
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.transactions.transaction

object TypeRacerDAO {

    private suspend fun post(user: User): TypeRacerRow {
        val entity = UserDAO.computeIfAbsent(user)
        return get(entity) ?: transaction {
            TypeRacerRow.new {
                userId = entity
                pb = 0f
                latest = 0f
            }
        }
    }

    suspend fun putPersonalBest(user: User, value: Float) {
        val row = post(user)
        transaction {
            row.pb = value
        }
    }

    suspend fun putLatest(user: User, value: Float) {
        val row = post(user)
        transaction {
            row.latest = value
        }
    }

    suspend fun getScoresMap(user: User): Map<String, Float> {
        val row = post(user)
        return transaction {
            mapOf(Pair("pb", row.pb), Pair("latest", row.latest))
        }
    }

    suspend fun get(user: UserRow): TypeRacerRow? = DataBaseFactory.query {
        transaction {
            TypeRacerRow.findById(user.id)
        }
    }

}