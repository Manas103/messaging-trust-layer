package com.manas.messagingtrust.detect

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidatorsTest {

    @Test
    fun registryHasExactlyFourteenUniquelyNamedValidators() {
        assertEquals(14, ValidatorRegistry.all.size)
        assertEquals(14, ValidatorRegistry.all.map { it.name }.toSet().size)
    }

    @Test
    fun usPhoneMatchesRealisticFormat() {
        assertEquals(1, UsPhoneValidator().find("call me at (415) 555-0199 anytime").size)
    }

    @Test
    fun ukPhoneMatchesRealisticFormat() {
        assertEquals(1, UkPhoneValidator().find("text 07911 123456 please").size)
    }

    @Test
    fun emailMatchesAnOrdinaryAddress() {
        assertEquals(1, EmailValidator().find("reach me at guest42@example.com today").size)
    }

    @Test
    fun creditCardValidatorRejectsAnInvalidLuhnNumber() {
        val valid = ValidInstanceGenerator.validLuhnCard(Random(42))
        val lastDigit = valid.last()
        val invalid = valid.dropLast(1) + ((lastDigit - '0' + 1) % 10)
        assertTrue(Checksums.luhnValid(valid))
        assertTrue(!Checksums.luhnValid(invalid))
        assertEquals(0, CreditCardLuhnValidator().find("$invalid is my card").size)
    }

    @Test
    fun creditCardValidatorAcceptsAGeneratedValidLuhnNumber() {
        val valid = ValidInstanceGenerator.validLuhnCard(Random(42))
        assertTrue(Checksums.luhnValid(valid))
        assertEquals(1, CreditCardLuhnValidator().find("card: $valid").size)
    }

    @Test
    fun ibanValidatorAcceptsAGeneratedValidIban() {
        val valid = ValidInstanceGenerator.validGermanIban(Random(7))
        assertTrue(Checksums.ibanValid(valid))
        assertEquals(1, IbanChecksumValidator().find("send to $valid please").size)
    }

    @Test
    fun ibanValidatorRejectsAShapeWithABadChecksum() {
        val valid = ValidInstanceGenerator.validGermanIban(Random(7))
        val lastDigit = valid.last()
        val invalid = valid.dropLast(1) + ((lastDigit - '0' + 1) % 10)
        assertTrue(Checksums.ibanValid(valid))
        assertTrue(!Checksums.ibanValid(invalid))
        assertEquals(0, IbanChecksumValidator().find(invalid).size)
    }

    @Test
    fun routingNumberValidatorRequiresBothTheKeywordAndAValidChecksum() {
        val valid = ValidInstanceGenerator.validAbaRouting(Random(3))
        assertEquals(0, UsRoutingNumberValidator().find("here is a number: $valid").size, "no 'routing' keyword nearby")
        assertEquals(1, UsRoutingNumberValidator().find("my routing number is $valid").size)
    }

    @Test
    fun ssnValidatorRejectsTheReservedAreaNumbers() {
        assertEquals(0, UsSsnFormatValidator().find("000-12-3456").size)
        assertEquals(0, UsSsnFormatValidator().find("666-12-3456").size)
        assertEquals(1, UsSsnFormatValidator().find("123-45-6789").size)
    }

    @Test
    fun offPlatformUrlValidatorIgnoresAirbnbItself() {
        assertEquals(0, OffPlatformUrlValidator().find("see https://www.airbnb.com/rooms/123").size)
        assertEquals(1, OffPlatformUrlValidator().find("see https://booking-direct.example.com/room").size)
    }

    @Test
    fun streetAddressValidatorMatchesACommonShape() {
        assertEquals(1, StreetAddressValidator().find("meet me at 221 Baker Street").size)
    }
}
