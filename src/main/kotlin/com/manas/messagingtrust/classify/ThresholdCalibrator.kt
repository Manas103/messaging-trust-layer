package com.manas.messagingtrust.classify

/**
 * Picks the TOP-band threshold as the score at the {@code (1 - topFraction)} percentile of a
 * calibration set's scores, so "the top risk band" means what it says: the riskiest slice of
 * traffic by the classifier's own score distribution, not a fixed score cutoff chosen by hand.
 */
object ThresholdCalibrator {
    fun percentileThreshold(scores: List<Double>, topFraction: Double): Double {
        if (scores.isEmpty()) return 1.0
        val sorted = scores.sorted()
        val index = ((1.0 - topFraction) * sorted.size).toInt().coerceIn(0, sorted.size - 1)
        return sorted[index]
    }
}
