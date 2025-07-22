package com.qbtech.com.qtest.service

import com.qbtech.com.qtest.models.BenfordResponse
import com.qtest.exceptions.BenfordExceptions
import org.apache.commons.math3.stat.inference.ChiSquareTest
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.log10

class BenfordService {

    fun calculateExpectedDistribution(): Map<Int, BigDecimal> {
        return (1..9).associateWith { digit ->
            logBenfordPercentage(digit)
        }
    }

    private fun logBenfordPercentage(digit: Int): BigDecimal {
        val percentage = log10(1 + 1.0 / digit) * 100
        return BigDecimal(percentage).setScale(2, RoundingMode.HALF_UP)
    }


    fun analyze(input: String, significanceLevel: Double): BenfordResponse {
        val digits = extractLeadingDigits(input)
        if (digits.size < 5) {
            throw BenfordExceptions.InsufficientDataException("Not enough numeric values to analyze")
        }
        val actualCounts = digits.groupingBy { it }.eachCount().mapValues { it.value.toLong() }
        val total = digits.size.toLong()

        if (total == 0L) {
            return BenfordResponse(
                actualDistribution = (1..9).associateWith { 0L },
                expectedDistribution = calculateExpectedDistribution(),
                chiSquareStatistic = 0.0,
                pValue = 1.0,
                conformsToBenford = false
            )
        }

        val expectedDist: Map<Int, BigDecimal> = calculateExpectedDistribution()

        val expectedCounts = (1..9).map { digit ->
            val percentage = expectedDist[digit] ?: BigDecimal.ZERO
            percentage.multiply(BigDecimal(total)).divide(BigDecimal(100))
        }.map { it.toDouble() }.toDoubleArray()

        val observedCounts = (1..9).map { digit ->
            actualCounts[digit] ?: 0L
        }.toLongArray()

        val chiTest = ChiSquareTest()
        val chiSq = chiTest.chiSquare(expectedCounts, observedCounts)
        val pVal = chiTest.chiSquareTest(expectedCounts, observedCounts)

        return BenfordResponse(
            actualDistribution = (1..9).associateWith { actualCounts[it] ?: 0L },
            expectedDistribution = expectedDist,
            chiSquareStatistic = chiSq,
            pValue = pVal,
            conformsToBenford = pVal > significanceLevel
        )
    }


    fun extractLeadingDigits(input: String): List<Int> {
        val regex = Regex("\\b\\d+(?:\\.\\d+)?\\b")
        return regex.findAll(input)
            .mapNotNull { match ->
                match.value.trimStart('0')
                    .firstOrNull { it.isDigit() && it != '0' }?.toString()?.toIntOrNull()
            }.toList()
    }
}
