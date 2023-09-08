package michi.bot.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable: IntIdTable("user", "uuid") {
    val userId = long("user_id").uniqueIndex()
    val preferredLanguage = varchar("preferred_language", 5)
}