package michi.bot.util

/**
 * Enum containing all the supported languages of the bot.
 */
enum class Language(val value: String) {
    PT_BR("pt-br"), EN_US("en-us");

    companion object {

        /**
         * Searches for an ENUM constant with a specific [value].
         * @return The [Language] with the specified value or null if it's unable to find one.
         * @author Slz
         */
        fun valueOfOrNull(value: String?): Language? = Language.values().find { language -> language.value == value?.lowercase() }

    }

}