package com.manas.messagingtrust.model

/** One simulated host-guest message. There is no real host, guest, or conversation behind any instance of this. */
data class Message(
    val id: String,
    val text: String,
    val locale: MessageLocale,
    val fromHost: Boolean
)

/** A detector or validator hit: the matched span plus which check produced it. */
data class Hit(val checkName: String, val start: Int, val end: Int, val matchedText: String)

enum class RiskBand { LOW, MEDIUM, HIGH, TOP }

data class ScreeningResult(
    val message: Message,
    val dictionaryHits: List<Hit>,
    val validatorHits: List<Hit>,
    val classifierScore: Double,
    val riskBand: RiskBand,
    val challenged: Boolean,
    val redactedText: String
)
