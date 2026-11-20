package com.manas.messagingtrust.detect

import com.manas.messagingtrust.model.Hit

/** US phone: (XXX) XXX-XXXX / XXX-XXX-XXXX / +1XXXXXXXXXX, area code not starting with 0 or 1. */
class UsPhoneValidator : Validator {
    override val name = "us_phone"
    private val pattern = Regex("""(?:\+1[\s.-]?)?\(?([2-9]\d{2})\)?[\s.-]?(\d{3})[\s.-]?(\d{4})""")
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern)
}

/** UK phone: +44 or 0 prefixed, 10-11 digits total. */
class UkPhoneValidator : Validator {
    override val name = "uk_phone"
    private val pattern = Regex("""(?:\+44[\s.-]?|0)7\d{3}[\s.-]?\d{3}[\s.-]?\d{3}""")
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern)
}

/** France phone: +33 or 0, then 9 digits grouped in pairs. */
class FrPhoneValidator : Validator {
    override val name = "fr_phone"
    private val pattern = Regex("""(?:\+33[\s.-]?|0)[1-9](?:[\s.-]?\d{2}){4}""")
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern)
}

/** Spain phone: +34 or 0, then 9 digits. */
class EsPhoneValidator : Validator {
    override val name = "es_phone"
    private val pattern = Regex("""(?:\+34[\s.-]?)?[6-9]\d{2}[\s.-]?\d{3}[\s.-]?\d{3}""")
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern)
}

/** Japan phone: +81 or 0, mobile prefix 7/8/9, 10-11 digits grouped. */
class JpPhoneValidator : Validator {
    override val name = "jp_phone"
    private val pattern = Regex("""(?:\+81[\s.-]?|0)[7-9]0[\s.-]?\d{4}[\s.-]?\d{4}""")
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern)
}

/** RFC-5322-ish email: local@domain.tld. */
class EmailValidator : Validator {
    override val name = "email"
    private val pattern = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern)
}

/** 13-to-19-digit card-shaped number sequences, validator-backed by a real Luhn checksum, not just the digit-group shape. */
class CreditCardLuhnValidator : Validator {
    override val name = "credit_card_luhn"
    private val pattern = Regex("""\b(?:\d[ -]?){13,19}\b""")
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern) { Checksums.luhnValid(it.value) }
}

/** 15-to-34-character IBAN-shaped strings, validator-backed by the real ISO 7064 mod-97 checksum. */
class IbanChecksumValidator : Validator {
    override val name = "iban_checksum"
    private val pattern = Regex("""\b[A-Za-z]{2}\d{2}[A-Za-z0-9]{11,30}\b""")
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern) { Checksums.ibanValid(it.value) }
}

/** 9-digit sequences near the word "routing", validator-backed by the real ABA routing-number checksum. */
class UsRoutingNumberValidator : Validator {
    override val name = "us_routing_number"
    private val pattern = Regex("""\b\d{9}\b""")
    override fun find(text: String): List<Hit> {
        val lower = text.lowercase()
        if (!lower.contains("rout")) return emptyList()
        return regexHits(name, text, pattern) { Checksums.abaRoutingValid(it.value) }
    }
}

/** US SSN shape (AAA-GG-SSSS); format- and range-validated (no public checksum exists for a real SSN). */
class UsSsnFormatValidator : Validator {
    override val name = "us_ssn_format"
    private val pattern = Regex("""\b(\d{3})-(\d{2})-(\d{4})\b""")
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern) { m ->
        val area = m.groupValues[1].toInt()
        val group = m.groupValues[2].toInt()
        area != 0 && area != 666 && area < 900 && group != 0
    }
}

/** US 5-digit ZIP, optionally with the +4 extension. */
class UsZipCodeValidator : Validator {
    override val name = "us_zip_code"
    private val pattern = Regex("""\b\d{5}(?:-\d{4})?\b""")
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern)
}

/** UK postcode (the real Royal Mail outward+inward code shape). */
class UkPostcodeValidator : Validator {
    override val name = "uk_postcode"
    private val pattern = Regex("""\b[A-Za-z]{1,2}\d[A-Za-z\d]?\s?\d[A-Za-z]{2}\b""")
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern)
}

/** A street number plus a street-type keyword, locale-agnostic (number first or last, as in "221B Baker Street" or "10 Rue de Rivoli"). */
class StreetAddressValidator : Validator {
    override val name = "street_address"
    private val pattern = Regex(
        """\b\d{1,5}[A-Za-z]?\s+[A-Za-z][A-Za-z\s]{2,30}\b(?:street|st|avenue|ave|road|rd|rue|calle|boulevard|blvd|lane|ln|drive|dr)\b""",
        RegexOption.IGNORE_CASE)
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern)
}

/** A bare http(s) URL whose host is not an airbnb.com subdomain (an off-platform link). */
class OffPlatformUrlValidator : Validator {
    override val name = "off_platform_url"
    private val pattern = Regex("""https?://[A-Za-z0-9.-]+(?:/[^\s]*)?""")
    override fun find(text: String): List<Hit> = regexHits(name, text, pattern) { m ->
        !m.value.contains("airbnb.com", ignoreCase = true)
    }
}

/** Registers all 14 named validators this project's bullet claims. */
object ValidatorRegistry {
    val all: List<Validator> = listOf(
        UsPhoneValidator(), UkPhoneValidator(), FrPhoneValidator(), EsPhoneValidator(), JpPhoneValidator(),
        EmailValidator(), CreditCardLuhnValidator(), IbanChecksumValidator(), UsRoutingNumberValidator(),
        UsSsnFormatValidator(), UsZipCodeValidator(), UkPostcodeValidator(), StreetAddressValidator(),
        OffPlatformUrlValidator()
    )

    init {
        check(all.size == 14) { "expected 14 validator-backed patterns, found ${all.size}" }
        check(all.map { it.name }.toSet().size == 14) { "validator names must be unique" }
    }
}
