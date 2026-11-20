package com.manas.messagingtrust.config

import com.manas.messagingtrust.classify.ClassifierTrainer
import com.manas.messagingtrust.classify.FeatureExtractor
import com.manas.messagingtrust.classify.LogisticClassifier
import com.manas.messagingtrust.classify.RiskBanding
import com.manas.messagingtrust.classify.ThresholdCalibrator
import com.manas.messagingtrust.detect.AhoCorasick
import com.manas.messagingtrust.detect.Dictionary
import com.manas.messagingtrust.detect.ValidatorRegistry
import com.manas.messagingtrust.generator.SyntheticMessageGenerator
import com.manas.messagingtrust.service.MessageScreeningService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.random.Random

@Configuration
class ScreeningConfig {

    @Bean
    fun ahoCorasick(): AhoCorasick = AhoCorasick(Dictionary.patterns)

    /**
     * Trained and calibrated at startup on a seeded synthetic set, so the application never
     * depends on an external model file. {@code app.screening.calibration-messages} controls how
     * much synthetic traffic is generated for this one-time startup calibration; the measured
     * 20,000-message precision/recall benchmark uses its own, larger, separate run (see
     * {@code PrecisionRecallBenchmarkRunner}), not this bean.
     */
    @Bean
    fun messageScreeningService(
        ahoCorasick: AhoCorasick,
        @Value("\${app.screening.calibration-messages:4000}") calibrationMessages: Int,
        @Value("\${app.screening.calibration-seed:11}") seed: Long,
        @Value("\${app.screening.top-fraction:0.031}") topFraction: Double
    ): MessageScreeningService {
        val generator = SyntheticMessageGenerator(Random(seed))
        val labeled = generator.generate(calibrationMessages)
        val classifier = ClassifierTrainer.train(labeled, ahoCorasick, ValidatorRegistry.all, epochs = 300, learningRate = 0.5)

        val scores = labeled.map { lm ->
            val dictHits = ahoCorasick.findAll(lm.message.text)
            val validatorHits = ValidatorRegistry.all.flatMap { it.find(lm.message.text) }
            classifier.score(FeatureExtractor.extract(lm.message, dictHits, validatorHits))
        }
        val topThreshold = ThresholdCalibrator.percentileThreshold(scores, topFraction)
        val highThreshold = ThresholdCalibrator.percentileThreshold(scores, 0.15)
        val mediumThreshold = ThresholdCalibrator.percentileThreshold(scores, 0.50)
        val banding = RiskBanding(mediumThreshold, highThreshold, topThreshold)

        return MessageScreeningService(ahoCorasick, ValidatorRegistry.all, classifier, banding)
    }
}
