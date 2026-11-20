package com.manas.messagingtrust.detect

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RedactorTest {

    @Test
    fun redactsAMatchedSpan() {
        val text = "call me at 4155550199 today"
        val hits = UsPhoneValidator().find(text)
        val redacted = Redactor.redact(text, hits)
        assertEquals("call me at [REDACTED] today", redacted)
    }

    @Test
    fun mergesOverlappingSpansIntoOnePlaceholder() {
        val text = "paypal me"
        val dictionary = AhoCorasick(listOf("pay", "paypal"))
        val hits = dictionary.findAll(text)
        val redacted = Redactor.redact(text, hits)
        assertEquals("[REDACTED] me", redacted)
    }

    @Test
    fun leavesCleanTextUnchanged() {
        val text = "thanks for the lovely stay"
        assertEquals(text, Redactor.redact(text, emptyList()))
    }

    @Test
    fun redactedTextNeverContainsTheOriginalSecret() {
        val text = "my number is (415) 555-0199, call anytime"
        val hits = UsPhoneValidator().find(text)
        val redacted = Redactor.redact(text, hits)
        assertFalse(redacted.contains("555-0199"))
    }
}
