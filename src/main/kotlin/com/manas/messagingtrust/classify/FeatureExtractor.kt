package com.manas.messagingtrust.classify

import com.manas.messagingtrust.model.Hit
import com.manas.messagingtrust.model.Message

/**
 * The small classifier's 5 features, all counts or booleans derived directly from the dictionary
 * and validator passes: how many distinct detectors fired, and whether certain detector families
 * fired, is already most of the signal a short message carries about redirect risk. A heavier
 * model (a transformer, even a bag-of-words classifier over the message text) was not needed to
 * separate the synthetic classes this project measures against; see README Findings.
 */
object FeatureExtractor {

    fun extract(message: Message, dictionaryHits: List<Hit>, validatorHits: List<Hit>): DoubleArray {
        val distinctDictionaryTerms = dictionaryHits.map { it.checkName }.toSet().size
        val distinctValidatorChecks = validatorHits.map { it.checkName }.toSet().size
        val hasPhoneOrEmail = validatorHits.any { it.checkName.contains("phone") || it.checkName == "email" }
        val hasFinancial = validatorHits.any { it.checkName.contains("credit_card") || it.checkName.contains("iban") || it.checkName.contains("routing") }
        val messageLengthNorm = (message.text.length.coerceAtMost(500)) / 500.0

        return doubleArrayOf(
            distinctDictionaryTerms.toDouble(),
            distinctValidatorChecks.toDouble(),
            if (hasPhoneOrEmail) 1.0 else 0.0,
            if (hasFinancial) 1.0 else 0.0,
            messageLengthNorm
        )
    }

    fun featureCount() = 5
}
