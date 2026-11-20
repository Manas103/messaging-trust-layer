package com.manas.messagingtrust.detect

/** Real checksum algorithms, not just format regexes, for the validators that have one. */
object Checksums {

    /** The Luhn algorithm (ISO/IEC 7812-1), used by every major card network. */
    fun luhnValid(digits: String): Boolean {
        val clean = digits.filter { it.isDigit() }
        if (clean.length < 12) return false
        var sum = 0
        var alternate = false
        for (i in clean.length - 1 downTo 0) {
            var n = clean[i] - '0'
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    /** IBAN mod-97 checksum (ISO 7064), as specified by ISO 13616: move the first 4 chars to the
     * end, map letters to numbers (A=10..Z=35), and the whole numeric string must be divisible by 97. */
    fun ibanValid(raw: String): Boolean {
        val clean = raw.replace(" ", "").uppercase()
        if (clean.length < 15 || clean.length > 34) return false
        if (!clean.take(2).all { it.isLetter() }) return false
        val rearranged = clean.substring(4) + clean.substring(0, 4)
        val numeric = StringBuilder()
        for (c in rearranged) {
            if (c.isDigit()) {
                numeric.append(c)
            } else if (c.isLetter()) {
                numeric.append(c - 'A' + 10)
            } else {
                return false
            }
        }
        var remainder = 0
        for (c in numeric) {
            remainder = (remainder * 10 + (c - '0')) % 97
        }
        return remainder == 1
    }

    /** The ABA routing-number checksum: 3*(d1+d4+d7) + 7*(d2+d5+d8) + 1*(d3+d6+d9) must be divisible by 10. */
    fun abaRoutingValid(digits: String): Boolean {
        val clean = digits.filter { it.isDigit() }
        if (clean.length != 9) return false
        val d = clean.map { it - '0' }
        val sum = 3 * (d[0] + d[3] + d[6]) + 7 * (d[1] + d[4] + d[7]) + 1 * (d[2] + d[5] + d[8])
        return sum % 10 == 0
    }
}
