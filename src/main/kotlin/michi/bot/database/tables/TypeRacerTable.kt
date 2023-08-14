package michi.bot.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TypeRacerTable: IntIdTable("type_racer", "uuid") {
    val userId = reference("user_id", UserTable.userId).uniqueIndex()
    val pb = float("personal_best")
    val latest = float("latest")
}