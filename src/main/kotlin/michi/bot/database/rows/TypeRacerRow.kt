package michi.bot.database.rows

import michi.bot.database.tables.TypeRacerTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TypeRacerRow(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<TypeRacerRow>(TypeRacerTable)

    var userId by (UserRow referencedOn TypeRacerTable.userId)
    var pb by TypeRacerTable.pb
    var latest by TypeRacerTable.latest
}