package michi.bot.database.rows

import michi.bot.database.tables.GuildTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

open class GuildRow(guildID: EntityID<Int>): IntEntity(guildID) {
    companion object : IntEntityClass<GuildRow>(GuildTable)

    var guildID by GuildTable.guildID
    var name by GuildTable.name
    var ownerID by GuildTable.ownerID
    var logsChannelID by GuildTable.logsChannelID
    var language by GuildTable.language
    var djs by GuildTable.djs
    var musicQueue by GuildTable.musicQueue

}