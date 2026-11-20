package com.manas.messagingtrust.service

import com.manas.messagingtrust.classify.FeatureExtractor
import com.manas.messagingtrust.classify.LogisticClassifier
import com.manas.messagingtrust.classify.RiskBanding
import com.manas.messagingtrust.detect.AhoCorasick
import com.manas.messagingtrust.detect.Redactor
import com.manas.messagingtrust.detect.ValidatorRegistry
import com.manas.messagingtrust.model.Message
import com.manas.messagingtrust.model.ScreeningResult

/**
 * The full pipeline a message goes through: the Aho-Corasick dictionary pass, the 14
 * validator-backed patterns, the small classifier, risk banding, and unconditional redaction into
 * the training copy. {@code challenged} (only TOP band) is the one decision this service makes
 * that has a user-facing effect; everything else is scoring and storage.
 */
class MessageScreeningService(
    private val dictionary: AhoCorasick,
    private val validators: List<com.manas.messagingtrust.detect.Validator> = ValidatorRegistry.all,
    private val classifier: LogisticClassifier,
    private val riskBanding: RiskBanding
) {
    fun screen(message: Message): ScreeningResult {
        val dictionaryHits = dictionary.findAll(message.text)
        val validatorHits = validators.flatMap { it.find(message.text) }
        val features = FeatureExtractor.extract(message, dictionaryHits, validatorHits)
        val score = classifier.score(features)
        val band = riskBanding.bandFor(score)
        val challenged = band == com.manas.messagingtrust.model.RiskBand.TOP
        val redactedText = Redactor.redact(message.text, dictionaryHits + validatorHits)
        return ScreeningResult(message, dictionaryHits, validatorHits, score, band, challenged, redactedText)
    }
}
