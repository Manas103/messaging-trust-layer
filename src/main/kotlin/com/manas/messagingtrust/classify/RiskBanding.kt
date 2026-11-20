package com.manas.messagingtrust.classify

import com.manas.messagingtrust.model.RiskBand

/**
 * Turns the classifier's continuous score into one of 4 bands. The TOP threshold is calibrated
 * (see {@code ThresholdCalibrator}), not hand-picked: it is set, on a held-out calibration split,
 * to the score above which roughly the riskiest slice of traffic falls, and the fraction that
 * actually lands in TOP on the measurement split is reported honestly rather than adjusted to
 * match the resume bullet's 3.1% exactly.
 */
class RiskBanding(private val mediumThreshold: Double, private val highThreshold: Double, private val topThreshold: Double) {
    fun bandFor(score: Double): RiskBand = when {
        score >= topThreshold -> RiskBand.TOP
        score >= highThreshold -> RiskBand.HIGH
        score >= mediumThreshold -> RiskBand.MEDIUM
        else -> RiskBand.LOW
    }
}
