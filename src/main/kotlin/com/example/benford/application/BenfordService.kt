package com.example.benford.application

import com.example.benford.models.BenfordResponse
import com.example.benford.infrastructure.ChiSquareEvaluator
import com.example.benford.util.BenfordAnalyzer
import java.math.BigDecimal
import java.math.RoundingMode

class BenfordService {

    fun analyze(input: String, significanceLevel: Double): BenfordResponse {
        require(input.isNotBlank()) {"Input is empty or missing numbers"}
        require(significanceLevel in 0.0..1.0) {"Significance level must be between 0 and 1"}

        val digits = BenfordAnalyzer.extractLeadingDigits(input)
        require(digits.size >= 5) { "Not enough numeric values to analyze" }
        val actualCounts = digits.groupingBy { it }.eachCount().mapValues { it.value.toLong() }

        val expectedDistribution: Map<Int, BigDecimal> = BenfordAnalyzer.BenfordExpectedDistribution.distribution

        val total = BigDecimal(digits.size)
        val expectedCounts = (1..9).map { digit ->
            expectedDistribution.getOrDefault(digit, BigDecimal.ZERO)
                .multiply(total)
                .setScale(6, RoundingMode.HALF_UP)
                .toDouble()
        }.toDoubleArray()

        val observedCounts = (1..9).map { actualCounts[it] ?: 0L }.toLongArray()

        val (chiSquareStats, pVal) = ChiSquareEvaluator.evaluate(expectedCounts, observedCounts)

        return BenfordResponse(
            actualDistribution = (1..9).associateWith { actualCounts[it] ?: 0L },
            expectedDistribution = expectedDistribution,
            chiSquareStatistic = chiSquareStats,
            pValue = pVal,
            conformsToBenford = pVal > significanceLevel
        )
    }

}
