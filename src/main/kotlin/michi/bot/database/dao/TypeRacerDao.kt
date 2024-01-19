package michi.bot.database.dao

import michi.bot.database.DataBaseFactory
import michi.bot.database.rows.TypeRacerRow
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.transactions.transaction

object TypeRacerDao {

    private suspend fun post(user: User): TypeRacerRow {
        val entity = UserDao.postIfAbsent(user)

        return get(user) ?: transaction {
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

    suspend fun get(user: User): TypeRacerRow? = DataBaseFactory.query {
        transaction {
            TypeRacerRow.all().find { it.userId.userId == user.idLong }
        }
    }

}