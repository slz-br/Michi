package michi.bot.commands.misc

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import michi.bot.commands.CommandScope.GLOBAL_SCOPE
import michi.bot.commands.MichiArgument
import michi.bot.commands.MichiCommand
import michi.bot.config
import michi.bot.listeners.CommandListener.json
import michi.bot.util.Emoji
import michi.bot.util.ReplyUtils.getText
import michi.bot.util.ReplyUtils.getYML
import michi.bot.util.ReplyUtils.michiReply
import michi.bot.util.ReplyUtils.michiSendMessage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.apache.maven.surefire.shared.lang3.StringUtils

@Suppress("Unused")
object Weather: MichiCommand("weather", GLOBAL_SCOPE) {

    override val descriptionLocalization: Map<DiscordLocale, String>
        get() = mapOf(
            DiscordLocale.ENGLISH_US to "Gives you info about the weather of a city",
            DiscordLocale.ENGLISH_UK to "Gives you info about the weather of a city",
            DiscordLocale.PORTUGUESE_BRAZILIAN to "Te dá informações do clima de uma cidade"
        )

    private const val BASE_URL = "http://api.weatherapi.com/v1"
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override val arguments = listOf(
        MichiArgument(
            name = "city",
            descriptionLocalization = mapOf(
                DiscordLocale.ENGLISH_US to "The name of the city to look the weather",
                DiscordLocale.ENGLISH_UK to "The name of the city to look the weather",
                DiscordLocale.PORTUGUESE_BRAZILIAN to "O nome da cidade para ver o clima"
            ),
            type = OptionType.STRING
        )
    )

    override val usage: String
        get() = "/$name <city>"

    override suspend fun execute(context: SlashCommandInteractionEvent) {
        val hook = context.hook
        val sender = context.user
        val city = context.getOption("city")!!.asString
        val cityNormalized = StringUtils.stripAccents(city)
            .replace(' ', '_')
            .split('_')
            .joinToString("_") {
                it.replaceFirstChar(Char::uppercase)
            }
        val err: YamlMap = getYML(sender).yamlMap["error_messages"]!!
        val miscErr: YamlMap = err["misc"]!!
        context.deferReply(true).queue()

        if (!canHandle(context)) return

        val response = client.get("$BASE_URL/current.json?key=${config["WEATHER_API_KEY"]}&q=$cityNormalized")

        if (response.status.value == 200) {
            val body = response.bodyAsText()

            val location = json.parseToJsonElement(body).jsonObject["location"]!!
            val current = json.parseToJsonElement(body).jsonObject["current"]!!
            val condition = current.jsonObject["condition"]!!

            val country = location.jsonObject["country"].toString().removeSurrounding("\"")
            val icon = "https:${condition.jsonObject["icon"].toString().removeSurrounding("\"")}"

            val wind = hashMapOf(
                "kilometer" to current.jsonObject["wind_kph"],
                "mile" to current.jsonObject["wind_mph"]
            )

            val temperature = hashMapOf(
                "celsius" to current.jsonObject["temp_c"],
                "fahrenheit" to current.jsonObject["temp_f"],
                "feelslike_celsius" to current.jsonObject["feelslike_c"],
                "feelslike_fahrenheit" to current.jsonObject["feelslike_f"]
            )

            val time = hashMapOf(
                "localTime" to location.jsonObject["localtime"].toString().removeSurrounding("\""),
                "lastUpdated" to current.jsonObject["last_updated"].toString().removeSurrounding("\"")
            )

            val success: YamlMap = getYML(sender).yamlMap["success_messages"]!!
            val miscSuccess: YamlMap = success["misc"]!!

            EmbedBuilder().apply {
                setTitle("$country/${cityNormalized.replace('_', ' ')}'s Weather")
                setThumbnail(icon)
                addField(
                    miscSuccess.getText("weather_temperature_info").split('\n')[0],
                    String.format(
                        "${miscSuccess.getText("weather_temperature_info").split("\n")[1]}\n${miscSuccess.getText("weather_temperature_info").split('\n')[2]}",
                        temperature["celsius"].toString(),
                        temperature["fahrenheit"].toString()
                    ),
                    true
                )
                addBlankField(true)
                addField(
                    miscSuccess.getText("weather_feels_like_info").split('\n')[0],
                    String.format(
                        "${miscSuccess.getText("weather_feels_like_info").split('\n')[1]}\n${miscSuccess.getText("weather_feels_like_info").split('\n')[2]}",
                        temperature["feelslike_celsius"].toString(),
                        temperature["feelslike_fahrenheit"].toString()
                    ),
                    true
                )
                addField(
                    miscSuccess.getText("weather_wind_info").split("\n")[0],
                    String.format(
                        "${miscSuccess.getText("weather_wind_info").split('\n')[1]}\n" +
                        miscSuccess.getText("weather_wind_info").split('\n')[2],
                        wind["kilometer"].toString(),
                        wind["mile"].toString()
                    ),
                    true
                )
                setFooter(String.format(miscSuccess.getText("weather_time_info"), time["lastUpdated"], time["localTime"]))
            }.build()
                .let(hook::sendMessageEmbeds)
                .setEphemeral(true)
                .queue()
        }
        else {
            hook.michiSendMessage(miscErr.getText("invalid_city_name"))
        }
    }

    override suspend fun canHandle(context: SlashCommandInteractionEvent): Boolean {
        val bot = context.guild?.selfMember

        val err: YamlMap = getYML(context.user).yamlMap["error_messages"]!!
        val genericErr: YamlMap = err["generic"]!!

        bot?.let {
            if (!bot.permissions.containsAll(super.botPermissions)) {
                context.michiReply(String.format(genericErr.getText("bot_missing_perms"), Emoji.michiSad))
                return false
            }
        }

        return true
    }

}