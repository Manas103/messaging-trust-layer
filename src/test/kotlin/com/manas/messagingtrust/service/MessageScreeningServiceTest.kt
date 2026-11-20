package com.manas.messagingtrust.service

import com.manas.messagingtrust.classify.ClassifierTrainer
import com.manas.messagingtrust.classify.RiskBanding
import com.manas.messagingtrust.classify.ThresholdCalibrator
import com.manas.messagingtrust.detect.AhoCorasick
import com.manas.messagingtrust.detect.Dictionary
import com.manas.messagingtrust.detect.ValidatorRegistry
import com.manas.messagingtrust.generator.SyntheticMessageGenerator
import com.manas.messagingtrust.model.Message
import com.manas.messagingtrust.model.MessageLocale
import com.manas.messagingtrust.model.RiskBand
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MessageScreeningServiceTest {

    private fun buildService(): MessageScreeningService {
        val dictionary = AhoCorasick(Dictionary.patterns)
        val labeled = SyntheticMessageGenerator(Random(5)).generate(2000)
        val classifier = ClassifierTrainer.train(labeled, dictionary, ValidatorRegistry.all, epochs = 300, learningRate = 0.5)
        val scores = labeled.map { lm ->
            val d = dictionary.findAll(lm.message.text)
            val v = ValidatorRegistry.all.flatMap { it.find(lm.message.text) }
            classifier.score(com.manas.messagingtrust.classify.FeatureExtractor.extract(lm.message, d, v))
        }
        val top = ThresholdCalibrator.percentileThreshold(scores, 0.031)
        val high = ThresholdCalibrator.percentileThreshold(scores, 0.15)
        val medium = ThresholdCalibrator.percentileThreshold(scores, 0.50)
        return MessageScreeningService(dictionary, ValidatorRegistry.all, classifier, RiskBanding(medium, high, top))
    }

    @Test
    fun aMessageWithAnObviousPaymentRedirectScoresHigherThanCleanChat() {
        val service = buildService()
        val risky = service.screen(Message("m1", "pay me directly on venmo, my number is (415) 555-0199", MessageLocale.US_EN, true))
        val clean = service.screen(Message("m2", "thanks so much for the lovely stay!", MessageLocale.US_EN, true))
        assertTrue(risky.classifierScore > clean.classifierScore)
    }

    @Test
    fun redactionNeverLeaksTheRawPhoneNumber() {
        val service = buildService()
        val result = service.screen(Message("m3", "call me at (415) 555-0199", MessageLocale.US_EN, false))
        assertFalse(result.redactedText.contains("555-0199"))
    }

    @Test
    fun onlyTopBandIsChallenged() {
        val service = buildService()
        val result = service.screen(Message("m4", "thanks for the lovely stay!", MessageLocale.US_EN, true))
        if (result.riskBand != RiskBand.TOP) {
            assertFalse(result.challenged)
        }
        assertEquals(result.riskBand == RiskBand.TOP, result.challenged)
    }

    @Test
    fun cleanMessageIsNeverChallenged() {
        val service = buildService()
        val result = service.screen(Message("m5", "what time is check-in tomorrow?", MessageLocale.US_EN, false))
        assertFalse(result.challenged)
    }
}
