package com.manas.messagingtrust.integration

import com.manas.messagingtrust.controller.ReviewDecisionRequest
import com.manas.messagingtrust.controller.SubmitMessageRequest
import com.manas.messagingtrust.model.MessageLocale
import com.manas.messagingtrust.model.MessageRecord
import com.manas.messagingtrust.model.ReviewQueueEntry
import com.manas.messagingtrust.model.ReviewStatus
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessagingIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var rest: TestRestTemplate

    @Test
    fun aRiskyMessageIsRedactedAndHeldForReviewWhenChallenged() {
        // repeated financial/contact redirects, likely to land in the top risk band
        val text = "pay me directly on venmo, my routing number is 021000021, here's my card 4532015112830366, call me at (415) 555-0199 instead of the app"
        val submitted = rest.postForObject(
            "http://localhost:$port/messages",
            SubmitMessageRequest("int-test-1", text, MessageLocale.US_EN, true),
            MessageRecord::class.java
        )
        assertNotNull(submitted)
        assertTrue(!submitted.redactedText.contains("555-0199"))
        assertTrue(!submitted.redactedText.contains("venmo", ignoreCase = true) || submitted.redactedText.contains("[REDACTED]"))

        if (submitted.challenged) {
            val pendingBefore = rest.getForObject("http://localhost:$port/review-queue", Array<ReviewQueueEntry>::class.java)
            val entry = pendingBefore!!.first { it.messageId == "int-test-1" }
            assertEquals(ReviewStatus.PENDING, entry.status)

            val decided = rest.postForObject(
                "http://localhost:$port/review-queue/${entry.id}/decision",
                ReviewDecisionRequest(approve = true),
                ReviewQueueEntry::class.java
            )
            assertEquals(ReviewStatus.APPROVED, decided!!.status)
        }
    }

    @Test
    fun aCleanMessageIsNeverHeldForReview() {
        val submitted = rest.postForObject(
            "http://localhost:$port/messages",
            SubmitMessageRequest("int-test-2", "what time is check-in tomorrow?", MessageLocale.US_EN, false),
            MessageRecord::class.java
        )
        assertNotNull(submitted)
        assertTrue(!submitted.challenged)
    }
}
