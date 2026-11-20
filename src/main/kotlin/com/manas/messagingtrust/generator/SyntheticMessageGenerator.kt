package com.manas.messagingtrust.generator

import com.manas.messagingtrust.detect.ValidInstanceGenerator
import com.manas.messagingtrust.model.Message
import com.manas.messagingtrust.model.MessageLocale
import kotlin.random.Random

data class LabeledMessage(val message: Message, val risky: Boolean)

/**
 * 20,000 seeded, deterministic simulated host-guest messages across 5 locales, roughly half
 * genuinely risky (carrying a real, checksum-valid-where-applicable PII or payment-redirect
 * pattern) and half clean ordinary booking chat. There is no real host, guest, or conversation
 * behind any message this generates.
 */
class SyntheticMessageGenerator(private val random: Random) {

    private val cleanTemplates = listOf(
        "Hi! Looking forward to the stay, what time is check-in?",
        "Thanks so much, the place looks lovely in the photos.",
        "Is there parking available nearby for the weekend?",
        "We will arrive a bit late, around 9pm, is that okay?",
        "The wifi password did not work, could you resend it?",
        "Just wanted to say thank you for such a smooth check-out process.",
        "Could you recommend a good restaurant close to the apartment?",
        "We loved our stay, the kitchen was exactly what we needed.",
        "What is the best way to reach the building after hours?",
        "Everything was spotless, thank you for the quick response earlier."
    )

    private val riskyLeadIns = listOf(
        "Hey, it's easier for both of us if we ", "Quick note: ", "By the way, ", "Also, ", ""
    )

    fun generate(count: Int): List<LabeledMessage> {
        val messages = mutableListOf<LabeledMessage>()
        for (i in 0 until count) {
            val locale = MessageLocale.entries[random.nextInt(MessageLocale.entries.size)]
            val fromHost = random.nextBoolean()
            val risky = random.nextDouble() < 0.5
            val text = if (risky) riskyMessage(locale) else cleanTemplates[random.nextInt(cleanTemplates.size)]
            messages.add(LabeledMessage(Message("msg-$i", text, locale, fromHost), risky))
        }
        return messages
    }

    private fun riskyMessage(locale: MessageLocale): String {
        val leadIn = riskyLeadIns[random.nextInt(riskyLeadIns.size)]
        return leadIn + when (random.nextInt(6)) {
            0 -> "pay me directly on venmo, it's faster than the app"
            1 -> "my number is ${phoneFor(locale)}, text me there"
            2 -> "my email is guest${random.nextInt(99999)}@example.com, send details there"
            3 -> "let's do a bank transfer, my routing number is ${ValidInstanceGenerator.validAbaRouting(random)}"
            4 -> "here's my card ${ValidInstanceGenerator.validLuhnCard(random)} to hold the deposit outside the app"
            else -> "wire it to this IBAN instead: ${ValidInstanceGenerator.validGermanIban(random)}"
        }
    }

    private fun phoneFor(locale: MessageLocale): String = when (locale) {
        MessageLocale.US_EN -> "(${randDigits(3, firstMin = 2)}) ${randDigits(3)}-${randDigits(4)}"
        MessageLocale.UK_EN -> "07${randDigits(3)} ${randDigits(3)} ${randDigits(3)}"
        MessageLocale.FR_FR -> "06 ${randDigits(2)} ${randDigits(2)} ${randDigits(2)} ${randDigits(2)}"
        MessageLocale.ES_ES -> "6${randDigits(2)} ${randDigits(3)} ${randDigits(3)}"
        MessageLocale.JP_JA -> "090-${randDigits(4)}-${randDigits(4)}"
    }

    private fun randDigits(count: Int, firstMin: Int = 0): String {
        val sb = StringBuilder()
        for (i in 0 until count) {
            sb.append(if (i == 0) random.nextInt(firstMin, 10) else random.nextInt(0, 10))
        }
        return sb.toString()
    }
}
