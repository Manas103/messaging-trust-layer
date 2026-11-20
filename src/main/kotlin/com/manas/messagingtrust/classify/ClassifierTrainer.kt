package com.manas.messagingtrust.classify

import com.manas.messagingtrust.detect.AhoCorasick
import com.manas.messagingtrust.detect.Validator
import com.manas.messagingtrust.generator.LabeledMessage

object ClassifierTrainer {
    fun train(
        labeledMessages: List<LabeledMessage>,
        dictionary: AhoCorasick,
        validators: List<Validator>,
        epochs: Int,
        learningRate: Double
    ): LogisticClassifier {
        val featureRows = mutableListOf<DoubleArray>()
        val labels = mutableListOf<Int>()
        for (labeled in labeledMessages) {
            val dictionaryHits = dictionary.findAll(labeled.message.text)
            val validatorHits = validators.flatMap { it.find(labeled.message.text) }
            featureRows.add(FeatureExtractor.extract(labeled.message, dictionaryHits, validatorHits))
            labels.add(if (labeled.risky) 1 else 0)
        }
        val classifier = LogisticClassifier(FeatureExtractor.featureCount())
        classifier.train(featureRows, labels, epochs, learningRate)
        return classifier
    }
}
