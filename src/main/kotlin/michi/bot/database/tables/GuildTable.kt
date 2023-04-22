package michi.bot.database.tables

import org.jetbrains.exposed.sql.Table

object GuildTable: Table("guilds") {

    val id = long("id")
    val name = varchar("guild_name", 100)
    val ownerID = long("owner_id")
    val iconURL = text("icon_url").nullable()

    override val primaryKey = PrimaryKey(id)

}