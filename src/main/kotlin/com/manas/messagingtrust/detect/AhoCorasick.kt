package com.manas.messagingtrust.detect

import com.manas.messagingtrust.model.Hit
import java.util.LinkedList

/**
 * A real Aho-Corasick automaton: a trie of the dictionary's patterns, a failure link per node
 * (computed by one BFS pass, the standard construction), and an output list per node merged with
 * its failure chain's outputs so a pattern that is a suffix of another is never missed. Scanning
 * a message is one pass over its characters, O(text length), regardless of how many patterns are
 * in the dictionary; this is the whole reason to build this instead of running N independent
 * `String.contains` checks.
 */
class AhoCorasick(patterns: List<String>) {

    private class Node {
        val children = HashMap<Char, Node>()
        var fail: Node? = null
        var patternsEndingHere: MutableList<String> = mutableListOf()
    }

    private val root = Node()

    init {
        for (pattern in patterns) {
            var node = root
            for (c in pattern.lowercase()) {
                node = node.children.getOrPut(c) { Node() }
            }
            node.patternsEndingHere.add(pattern.lowercase())
        }
        buildFailureLinks()
    }

    private fun buildFailureLinks() {
        val queue = LinkedList<Node>()
        for (child in root.children.values) {
            child.fail = root
            queue.add(child)
        }
        while (queue.isNotEmpty()) {
            val current = queue.poll()
            for ((c, child) in current.children) {
                queue.add(child)
                var failNode = current.fail
                while (failNode != null && !failNode.children.containsKey(c)) {
                    failNode = failNode.fail
                }
                child.fail = failNode?.children?.get(c) ?: root
                // Merge the fail chain's own matches so a pattern that is a suffix of another
                // (e.g. "pay" inside "paypal") is still reported at this node.
                child.patternsEndingHere.addAll(child.fail!!.patternsEndingHere)
            }
        }
    }

    /** Every dictionary pattern occurrence in {@code text}, lowercase-matched, with its original-case span. */
    fun findAll(text: String): List<Hit> {
        val lower = text.lowercase()
        val hits = mutableListOf<Hit>()
        var node = root
        for (i in lower.indices) {
            val c = lower[i]
            while (node !== root && !node.children.containsKey(c)) {
                node = node.fail ?: root
            }
            node = node.children[c] ?: root
            for (pattern in node.patternsEndingHere) {
                val start = i - pattern.length + 1
                hits.add(Hit("dictionary:$pattern", start, i + 1, text.substring(start, i + 1)))
            }
        }
        return hits
    }
}
