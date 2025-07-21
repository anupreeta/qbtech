
package com.qbtech

import com.qbtech.com.qtest.service.BenfordService
import java.math.BigDecimal
import kotlin.test.*


class BenfordServiceTest {
    private val service = BenfordService()

    @Test
    fun extractLeadingDigits_shouldWorkCorrectly() {
        val input = "100, 200.25, 030, 0.47, 0031.3"
        val result = service.extractLeadingDigits(input)
        assertEquals(listOf(1, 2, 3, 4, 3), result)
    }

    @Test
    fun extractLeadingDigits_shouldWork() {
        val input = "Invoice 4.56, 00456, 0.456, 0.00456, -4.56"
        val result = service.extractLeadingDigits(input)
        assertEquals(listOf(4, 4, 4, 4, 4), result)
    }

    @Test
    fun extractLeadingDigits_include_decimals() {
        val input = "0.0045, Glucose, 0.09, 0.81, 1.23, 2.34, 0.00007, 4.56, Hb, 9.99, 0.0000003, 7.89, Uric Acid, 6.7, 3.14"
        val result = service.extractLeadingDigits(input)
        assertEquals(listOf(4, 9, 8, 1, 2, 7, 4, 9, 3, 7, 6, 3), result)
    }

    @Test
    fun calculateExpectedDistribution_shouldReturnValidBenfordPercents() {
        val expected = service.calculateExpectedDistribution()


        assertEquals(9, expected.size)
        assertEquals(BigDecimal("30.1").toDouble(), expected[1]?.toDouble() ?: 0.0, 0.1)
        assertEquals(BigDecimal("17.61").toDouble(), expected[2]?.toDouble() ?: 0.0, 0.1)
        assertEquals(BigDecimal("4.6").toDouble(), expected[9]?.toDouble() ?: 0.0, 0.1)
        //assertEquals(17.6, expected[2] ?: 0.0, 0.1)
        //assertEquals(4.6, expected[9] ?: 0.0, 0.1)
        /*
        testExpectedDistributionHasCorrectValue(1, 30.1)
        testExpectedDistributionHasCorrectValue(2, 17.61)
        testExpectedDistributionHasCorrectValue(9, 4.6)

        assertEquals(30.1, expected[1]?.toDouble() ?: "Expected 30.1% for digit 1")
        assertEquals(17.61, expected[2]?.toDouble() ?: "Expected 17.6% for digit 2")
        assertEquals(4.6, expected[9]?.toDouble() ?: "Expected 4.6% for digit 9")
        assertEquals(30.1, expected[1] ?: 0.0, 0.1)
        assertEquals(17.6, expected[2] ?: 0.0, 0.1)
        assertEquals(4.6, expected[9] ?: 0.0, 0.1)*/

        val totalPercent = expected.values.reduce { acc, value -> acc + value }
        assertEquals(100.0, totalPercent.toDouble(), 0.1)
    }

    @Test
    fun analyze_shouldConformToBenford_givenRealisticInput() {
        val input = """
            123, 1567, 1900, 1450, 178, 10200, 113, 1040, 19800, 167,
            2300, 280, 290, 210, 250, 270, 223, 239, 265,
            330, 360, 390, 300, 310, 320, 345,
            450, 470, 410, 430, 490,
            520, 540, 580, 530,
            610, 650, 620, 690,
            730, 780,
            810, 860,
            920, 970
        """.trimIndent()

        val result = service.analyze(input, significanceLevel = 0.05)
        assertTrue(result.conformsToBenford)
        assertTrue(result.pValue > 0.05)
    }

    @Test
    fun analyze_shouldNotConformToBenford_givenEvenlyDistributedDigits() {
        val input = (100..900 step 10).joinToString(", ")
        val result = service.analyze(input, significanceLevel = 0.05)
        println("false test" + result)
        assertFalse(result.conformsToBenford)
        assertTrue(result.pValue < 0.05)
    }

    @Test
    fun analyze_shouldConformToBenford_givenUnEvenlyDistributedDigits() {
        val input = (100..900 step 100).joinToString(", ")
        val result = service.analyze(input, significanceLevel = 0.05)
        println("true test" + result)
        assertTrue(result.conformsToBenford)
        assertTrue(result.pValue > 0.05)
    }

    @Test
    fun analyze_shouldConformToBenford() {
        val input = listOf(
            123, 1450, 113, 19800, 167, 2300, 280,
            330, 360, 390, 450, 470, 520, 540, 610, 730, 810, 920
        ).joinToString(", ")
        val result = service.analyze(input, significanceLevel = 0.05)
        println("true test" + result)
        assertTrue(result.conformsToBenford)
        assertTrue(result.pValue > 0.05)
    }
}
