package michi.bot.database.rows

import michi.bot.database.tables.UserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserRow(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<UserRow>(UserTable)

    var userId by UserTable.userId
    var preferredLanguage by UserTable.preferredLanguage
}