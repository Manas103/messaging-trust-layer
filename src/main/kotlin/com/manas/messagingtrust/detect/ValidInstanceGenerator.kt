package com.manas.messagingtrust.detect

import kotlin.random.Random

/** Generates real, checksum-valid instances of the three validators that have a checksum, for the synthetic positive examples. */
object ValidInstanceGenerator {

    fun validLuhnCard(random: Random, length: Int = 16): String {
        val body = (1 until length).map { random.nextInt(0, 10) }
        var sum = 0
        var alternate = true // the check digit is at an even position counting from the right, so the digit just left of it is doubled first
        for (i in body.indices.reversed()) {
            var n = body[i]
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        val checkDigit = (10 - (sum % 10)) % 10
        return (body + checkDigit).joinToString("")
    }

    fun validAbaRouting(random: Random): String {
        val d = (0 until 8).map { random.nextInt(0, 10) }
        for (last in 0..9) {
            val full = d + last
            val sum = 3 * (full[0] + full[3] + full[6]) + 7 * (full[1] + full[4] + full[7]) + 1 * (full[2] + full[5] + last)
            if (sum % 10 == 0) {
                return full.joinToString("")
            }
        }
        error("unreachable: one of 0..9 always satisfies the mod-10 ABA checksum")
    }

    /** A valid German IBAN (DE + 2 check digits + 18-digit BBAN), computed by the real ISO 7064 mod-97 rule. */
    fun validGermanIban(random: Random): String {
        val bban = (0 until 18).map { random.nextInt(0, 10) }.joinToString("")
        val rearranged = bban + "DE" + "00"
        val numeric = StringBuilder()
        for (c in rearranged) {
            numeric.append(if (c.isDigit()) c else (c - 'A' + 10).toString())
        }
        var remainder = 0
        for (c in numeric) {
            remainder = (remainder.toString() + c).toBigInteger().mod(97.toBigInteger()).toInt()
        }
        val checkDigits = 98 - remainder
        return "DE" + checkDigits.toString().padStart(2, '0') + bban
    }
}
