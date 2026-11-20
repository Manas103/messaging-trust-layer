package com.manas.messagingtrust.detect

import com.manas.messagingtrust.model.Hit

/**
 * Replaces every detected span (dictionary or validator) with {@code [REDACTED]}, merging
 * overlapping spans first so a validator hit nested inside a dictionary hit (or vice versa)
 * produces one placeholder, not two overlapping ones. This is the function that runs before any
 * message reaches the training copy, unconditionally, regardless of the message's risk band.
 */
object Redactor {

    fun redact(text: String, hits: List<Hit>): String {
        if (hits.isEmpty()) return text
        val merged = mergeOverlapping(hits.sortedBy { it.start })
        val result = StringBuilder()
        var cursor = 0
        for (span in merged) {
            result.append(text, cursor, span.first)
            result.append("[REDACTED]")
            cursor = span.second
        }
        result.append(text, cursor, text.length)
        return result.toString()
    }

    private fun mergeOverlapping(sorted: List<Hit>): List<Pair<Int, Int>> {
        val merged = mutableListOf<Pair<Int, Int>>()
        for (hit in sorted) {
            if (merged.isEmpty() || hit.start > merged.last().second) {
                merged.add(hit.start to hit.end)
            } else {
                val last = merged.removeAt(merged.size - 1)
                merged.add(last.first to maxOf(last.second, hit.end))
            }
        }
        return merged
    }
}
