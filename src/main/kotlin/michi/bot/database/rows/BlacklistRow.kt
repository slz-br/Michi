package michi.bot.database.rows

import michi.bot.database.tables.BlacklistTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class BlacklistRow(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<BlacklistRow>(BlacklistTable)

    var type by BlacklistTable.type
    var entityID by BlacklistTable.entityID
    var reason by BlacklistTable.reason

}