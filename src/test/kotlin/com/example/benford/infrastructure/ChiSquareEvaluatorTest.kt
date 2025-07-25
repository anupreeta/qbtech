package com.example.benford.infrastructure

import com.example.benford.exceptions.BenfordException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class ChiSquareEvaluatorTest {

    @Test
    fun `should return valid statistic and pValue for correct input`() {
        val expected = doubleArrayOf(301.0, 176.0, 125.0, 97.0, 79.0, 67.0, 58.0, 51.0, 46.0)
        val observed = longArrayOf(298, 180, 120, 95, 80, 65, 60, 52, 50)

        val (statistic, pValue) = ChiSquareEvaluator.evaluate(expected, observed)

        assertEquals(0.87, statistic, 0.01)
        assertEquals(0.99, pValue, 0.01)
        assertTrue(statistic < 10.0, "Chi-square statistic should be small")
        assertTrue(pValue in 0.0..1.0, "pValue should be between 0 and 1")
    }

    @Test
    fun `should throw InvalidInputException for mismatched array sizes`() {
        val expected = doubleArrayOf(301.0, 176.0, 125.0)
        val observed = longArrayOf(300, 170)

        val ex = assertThrows<IllegalArgumentException> {
            ChiSquareEvaluator.evaluate(expected, observed)
        }
        assertTrue(ex.message!!.contains("same length"))
    }

    @Test
    fun `should throw InvalidInputException when expected contains zero`() {
        val expected = doubleArrayOf(301.0, 0.0, 125.0, 97.0, 79.0, 67.0, 58.0, 51.0, 46.0)
        val observed = longArrayOf(298, 180, 120, 95, 80, 65, 60, 52, 50)

        val ex = assertThrows<IllegalArgumentException> {
            ChiSquareEvaluator.evaluate(expected, observed)
        }
        assertTrue(ex.message!!.contains("greater than zero"))
    }

    @Test
    fun `should throw ChiSquareComputationException for invalid statistical input`() {
        val expected = doubleArrayOf(10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0)
        val observed = longArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0)

        val ex = assertThrows<BenfordException.ChiSquareComputationException> {
            ChiSquareEvaluator.evaluate(expected, observed)
        }
        assertTrue(ex.message!!.contains("Chi-Square test failed due to invalid input"))
    }
}