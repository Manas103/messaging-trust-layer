package com.manas.messagingtrust.detect

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AhoCorasickTest {

    @Test
    fun findsASinglePattern() {
        val automaton = AhoCorasick(listOf("venmo"))
        val hits = automaton.findAll("just pay me on venmo please")
        assertEquals(1, hits.size)
        assertEquals("venmo", hits[0].matchedText.lowercase())
    }

    @Test
    fun findsMultipleOverlappingPatternsInOnePass() {
        // "paypal" contains "pay" as a real prefix: both patterns must be reported.
        val automaton = AhoCorasick(listOf("pay", "paypal"))
        val hits = automaton.findAll("use paypal")
        val names = hits.map { it.checkName }.toSet()
        assertTrue(names.contains("dictionary:pay"))
        assertTrue(names.contains("dictionary:paypal"))
    }

    @Test
    fun isCaseInsensitive() {
        val automaton = AhoCorasick(listOf("whatsapp"))
        assertEquals(1, automaton.findAll("message me on WhatsApp").size)
    }

    @Test
    fun noFalseMatchOnUnrelatedText() {
        val automaton = AhoCorasick(Dictionary.patterns)
        assertEquals(0, automaton.findAll("Thanks for the lovely stay, see you next time!").size)
    }

    @Test
    fun matchedTextPreservesOriginalCase() {
        val automaton = AhoCorasick(listOf("telegram"))
        val hits = automaton.findAll("Let's chat on Telegram instead")
        assertEquals("Telegram", hits[0].matchedText)
    }
}
