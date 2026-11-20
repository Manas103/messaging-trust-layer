package com.manas.messagingtrust.classify

import kotlin.math.exp

/**
 * The "small classifier" the resume bullet names: plain logistic regression over
 * {@code FeatureExtractor}'s 5 counts/flags, trained by batch gradient descent. Combined with the
 * dictionary pass and the 14 validators, this is the third and last stage of the screening
 * pipeline; its only job is to turn "how many, and which kinds of, detectors fired" into one risk
 * score precision and recall are measured against.
 */
class LogisticClassifier(featureCount: Int) {
    private val weights = DoubleArray(featureCount)
    private var bias = 0.0

    fun score(features: DoubleArray): Double = sigmoid(dot(features))

    private fun dot(features: DoubleArray): Double {
        var sum = bias
        for (i in features.indices) sum += weights[i] * features[i]
        return sum
    }

    private fun sigmoid(x: Double): Double = 1.0 / (1.0 + exp(-x))

    fun train(featureRows: List<DoubleArray>, labels: List<Int>, epochs: Int, learningRate: Double) {
        val n = featureRows.size
        if (n == 0) return
        val featureCount = weights.size
        repeat(epochs) {
            val gradW = DoubleArray(featureCount)
            var gradB = 0.0
            for (i in 0 until n) {
                val features = featureRows[i]
                val prediction = sigmoid(dot(features))
                val error = prediction - labels[i]
                for (j in 0 until featureCount) gradW[j] += error * features[j]
                gradB += error
            }
            for (j in 0 until featureCount) weights[j] -= learningRate * gradW[j] / n
            bias -= learningRate * gradB / n
        }
    }

    fun weights(): DoubleArray = weights
    fun bias(): Double = bias
}
