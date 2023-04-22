package michi.bot.database.tables

import org.jetbrains.exposed.sql.Table

object BlacklistTable: Table("blacklist") {

    val type = varchar("type", 6)
    val id = long("id")
    val reason = text("reason").nullable()

    override val primaryKey = PrimaryKey(id)

}