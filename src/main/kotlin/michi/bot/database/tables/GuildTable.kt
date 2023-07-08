package michi.bot.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object GuildTable: IntIdTable("guilds", "position") {

    val guildID = long("guild_id")
    val name = varchar("guild_name", 100)
    val ownerID = long("owner_id")
    val iconURL = text("icon_url").nullable()
    val logsChannelID = long("logs_channel_id").nullable()
    val language = varchar("language", 6).nullable()
    val djs = text("djs").nullable()
    val musicQueue = largeText("music_queue").nullable()

}