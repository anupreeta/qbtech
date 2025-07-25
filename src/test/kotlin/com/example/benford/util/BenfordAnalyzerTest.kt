package com.example.benford.util

import com.example.benford.testUtil.SampleDataGenerator
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class BenfordAnalyzerTest {
    @Test
    fun `should extract leadingDigits correctly for mix of text and numbers with signs`() {
        val input = "Invoice, 100, 200.25, +4.56, 00456, Fees, 0.456, 0.00456, -4.56, 0031.3, 0.0000003"
        val result = BenfordAnalyzer.extractLeadingDigits(input)
        assertEquals(listOf(1, 2, 4, 4, 4, 4, 4, 3, 3), result)
    }

    @Test
    fun `should extract leadingDigits for dataset size of 350`() {
        val text = SampleDataGenerator.generateBenfordNumbersWithDecimals(350)
        val numbers = BenfordAnalyzer.extractLeadingDigits(text)

        assertEquals(350,numbers.size)
    }

    @Test
    fun `should extract leadingDigits for dataset size of 2000`() {
        val text = SampleDataGenerator.generateBenfordNumbersWithDecimals(2000)
        val numbers = BenfordAnalyzer.extractLeadingDigits(text)
        assertEquals(2000,numbers.size)
    }


    @Test
    fun `should return valid expected distributions`() {
        val expected = BenfordAnalyzer.calculateExpectedDistribution()

        assertEquals(9, expected.size)
        assertEquals(BigDecimal("30.10"), expected[1])
        assertEquals(BigDecimal("17.61"), expected[2])
        assertEquals(BigDecimal("12.49"), expected[3])
        assertEquals(BigDecimal("9.69"), expected[4])
        assertEquals(BigDecimal("7.92"), expected[5])
        assertEquals(BigDecimal("6.69"), expected[6])
        assertEquals(BigDecimal("5.80"), expected[7])
        assertEquals(BigDecimal("5.12"), expected[8])
        assertEquals(BigDecimal("4.58"), expected[9])

        val totalPercent = expected.values.reduce { acc, value -> acc + value }
        assertEquals(100.0, totalPercent.toDouble(), 0.1)
    }
}