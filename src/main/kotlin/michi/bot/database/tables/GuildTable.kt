package michi.bot.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object GuildTable: IntIdTable("guilds", "row_id") {

    val guildID = long("guild_id")
    val name = varchar("guild_name", 100)
    val ownerID = long("owner_id")
    val logsChannelID = long("logs_channel_id").nullable()
    val language = varchar("language", 6)
    val djs = text("djs").nullable()
    val musicQueue = largeText("music_queue")

}