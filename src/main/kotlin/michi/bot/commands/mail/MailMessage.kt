package michi.bot.commands.mail

import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentRequest
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentResponse
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType
import au.com.origma.perspectiveapi.v1alpha1.models.ContentType
import au.com.origma.perspectiveapi.v1alpha1.models.Entry
import michi.bot.perspectiveAPI
import net.dv8tion.jda.api.entities.UserSnowflake
import java.net.MalformedURLException
import java.net.URL

class MailMessage(val title: String,val message: String,val sender: UserSnowflake) {

    var isSafe: Boolean = false
        private set

    var containsLink: Boolean = false
        private set

    var unknownLanguage: Boolean = false
        private set

    init {

        if (this.message.split(" ").any { isURL(it) }) containsLink = true

        val request: AnalyzeCommentResponse? = perspectiveAPI!!.analyze(
            AnalyzeCommentRequest.Builder()
            .addRequestedAttribute(AttributeType.SEVERE_TOXICITY, null)
            .addRequestedAttribute(AttributeType.IDENTITY_ATTACK, null)
            .addRequestedAttribute(AttributeType.SEXUALLY_EXPLICIT, null)
            .doNotStore(true)
            .comment(
                Entry.Builder()
                .type(ContentType.PLAIN_TEXT)
                .text(this.message)
                .build()
            )
            .build()
        )

        if (request == null) {
            isSafe = false
            unknownLanguage = true
        }
        else {
            val severeToxicity =   request.getAttributeScore(AttributeType.SEVERE_TOXICITY).summaryScore.value
            val sexuallyExplicit = request.getAttributeScore(AttributeType.SEXUALLY_EXPLICIT).summaryScore.value
            val identityAttack =   request.getAttributeScore(AttributeType.IDENTITY_ATTACK).summaryScore.value

            isSafe = severeToxicity < 0.55f && sexuallyExplicit < 0.6f && identityAttack < 0.6f
        }
    }

    private fun isURL(message: String): Boolean = try { URL(message); true } catch (e: MalformedURLException) { false }

    override fun toString(): String = "**${this.title}**\n```${this.message}```"

}