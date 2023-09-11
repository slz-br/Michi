package michi.bot.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LanguageTest {

    /**
     * Checks if the [Language.valueOfOrNull] function returns [Language.EN_US]
     */
    @Test
    fun `should be english`() {
        assertEquals(Language.EN_US, Language.valueOfOrNull("en-us"))
    }

    /**
     * Checks if the [Language.valueOfOrNull] function returns [Language.PT_BR]
     */
    @Test
    fun `should be portuguese`() {
        assertEquals(Language.PT_BR, Language.valueOfOrNull("pt-br"))
    }

}