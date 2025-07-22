
package com.qbtech

import com.qbtech.com.qtest.service.BenfordService
import com.qtest.exceptions.BenfordExceptions
import java.math.BigDecimal
import kotlin.test.*


class BenfordServiceTest {
    private val service = BenfordService()

    @Test
    fun `extractLeadingDigits for mix of text and numbers with signs should work correctly`() {
        val input = "Invoice +4.56, 00456, Fees, 0.456, 0.00456, -4.56"
        val result = service.extractLeadingDigits(input)
        assertEquals(listOf(4, 4, 4, 4, 4), result)
    }

    @Test
    fun `extractLeadingDigits for mixed type of numbers should work correctly`() {
        val input = "100, 200.25, 030, 0.47, 0031.3"
        val result = service.extractLeadingDigits(input)
        assertEquals(listOf(1, 2, 3, 4, 3), result)
    }

    @Test
    fun `extractLeadingDigits including numbers, text and numbers work correctly`() {
        val input = "0.0045, Glucose, 0.09, 0.81, 1.23, 2.34, 0.00007, 4.56, Hb, 9.99, 0.0000003, 7.89, Uric Acid, 6.7, 3.14"
        val result = service.extractLeadingDigits(input)
        assertEquals(listOf(4, 9, 8, 1, 2, 7, 4, 9, 3, 7, 6, 3), result)
    }

    @Test
    fun `calculateExpectedDistribution should return valid expected distributions`() {
        val expected = service.calculateExpectedDistribution()

        assertEquals(9, expected.size)
        assertEquals(BigDecimal("30.1").toDouble(), expected[1]?.toDouble() ?: 0.0, 0.1)
        assertEquals(BigDecimal("17.61").toDouble(), expected[2]?.toDouble() ?: 0.0, 0.1)
        assertEquals(BigDecimal("12.49").toDouble(), expected[3]?.toDouble() ?: 0.0, 0.1)
        assertEquals(BigDecimal("9.69").toDouble(), expected[4]?.toDouble() ?: 0.0, 0.1)
        assertEquals(BigDecimal("7.92").toDouble(), expected[5]?.toDouble() ?: 0.0, 0.1)
        assertEquals(BigDecimal("6.69").toDouble(), expected[6]?.toDouble() ?: 0.0, 0.1)
        assertEquals(BigDecimal("5.80").toDouble(), expected[7]?.toDouble() ?: 0.0, 0.1)
        assertEquals(BigDecimal("5.12").toDouble(), expected[8]?.toDouble() ?: 0.0, 0.1)
        assertEquals(BigDecimal("4.58").toDouble(), expected[9]?.toDouble() ?: 0.0, 0.1)

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
    fun `analyze should conform to Benford given uniform distribution of leading digits and small sample size`() {
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

    @Test
    fun `analyze should throw InsufficientDataException when digits are fewer than 5`() {
        val input = "100, 200, 300" // Only 3 leading digits
        val significanceLevel = 0.05

        val exception = assertFailsWith<BenfordExceptions.InsufficientDataException> {
            service.analyze(input, significanceLevel)
        }

        assertEquals("Not enough numeric values to analyze", exception.message)
    }
}
