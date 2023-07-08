package michi.bot.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object BlacklistTable: IntIdTable("blacklist", "position") {

    val type = varchar("type", 6)
    val entityID = long("entity_id")
    val reason = text("reason").nullable()

}