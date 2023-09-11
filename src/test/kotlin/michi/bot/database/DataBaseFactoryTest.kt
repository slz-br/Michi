package michi.bot.database

import michi.bot.config
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class DataBaseFactoryTest {

    /**
     * Checks if it's possible to connect to the database with the
     * credentials present in the .env file.
     */
    @Test
    fun `database connected successfully`() {
        assertDoesNotThrow {
            Database.connect(
                url      = config["DB_URL"],
                driver   = config["DB_DRIVER"],
                user     = config["DB_USER"],
                password = config["DB_PASSWORD"]
            )
        }
    }

}