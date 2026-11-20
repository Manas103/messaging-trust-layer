package com.manas.messagingtrust.detect

import com.manas.messagingtrust.model.Hit

/** One independently named, independently testable detection rule. 14 implementations exist (see {@code ValidatorRegistry}). */
interface Validator {
    val name: String
    fun find(text: String): List<Hit>
}

/** Shared helper: builds a Hit list from every match of a compiled regex, filtered by an optional extra predicate. */
internal fun regexHits(name: String, text: String, pattern: Regex, extraCheck: (MatchResult) -> Boolean = { true }): List<Hit> {
    return pattern.findAll(text)
        .filter(extraCheck)
        .map { Hit(name, it.range.first, it.range.last + 1, it.value) }
        .toList()
}
