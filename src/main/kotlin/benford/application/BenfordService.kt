package benford.application

import benford.models.BenfordResponse
import benford.exceptions.BenfordException
import benford.infrastructure.ChiSquareEvaluator
import benford.util.BenfordAnalyzer
import java.math.BigDecimal

class BenfordService {

    fun analyze(input: String, significanceLevel: Double): BenfordResponse {

        val digits = BenfordAnalyzer.extractLeadingDigits(input)
        require(digits.size >= 5) {
            throw BenfordException.InsufficientDataException("Not enough numeric values to analyze")
        }

        val total = digits.size.toLong()
        val actualCounts = digits.groupingBy { it }.eachCount().mapValues { it.value.toLong() }
        println(actualCounts)
        val expectedDistribution = BenfordAnalyzer.calculateExpectedDistribution()

        val expectedCounts = (1..9).map { digit ->
            (expectedDistribution[digit] ?: BigDecimal.ZERO)
                .multiply(BigDecimal(total))
                .divide(BigDecimal(100))
                .toDouble()
        }.toDoubleArray()
        println(expectedCounts)
        val observedCounts = (1..9).map { actualCounts[it] ?: 0L }.toLongArray()

        val (chiSq, pVal) = ChiSquareEvaluator.evaluate(expectedCounts, observedCounts)

        return BenfordResponse(
            actualDistribution = observedCounts.withIndex().associate { (i, count) -> i + 1 to count },
            expectedDistribution = expectedDistribution,
            chiSquareStatistic = chiSq,
            pValue = pVal,
            conformsToBenford = pVal > significanceLevel
        )


        /*
        val digits = BenfordAnalyzer.extractLeadingDigits(input)
        if (digits.size < 5) {
            throw BenfordException.InsufficientDataException("Not enough numeric values to analyze")
        }
        val actualCounts = digits.groupingBy { it }.eachCount().mapValues { it.value.toLong() }
        val total = digits.size.toLong()

        val expectedDist = BenfordAnalyzer.calculateExpectedDistribution()

        val expectedCounts = (1..9).map { digit ->
            val percentage = expectedDist[digit] ?: BigDecimal.ZERO
            percentage.multiply(BigDecimal(total)).divide(BigDecimal(100))
        }.map { it.toDouble() }.toDoubleArray()

        val observedCounts = (1..9).map { digit ->
            actualCounts[digit] ?: 0L
        }.toLongArray()

        val (chiSq, pVal) = ChiSquareEvaluator.evaluate(expectedCounts, observedCounts)

        return BenfordResponse(
            actualDistribution = (1..9).associateWith { actualCounts[it] ?: 0L },
            expectedDistribution = expectedDist,
            chiSquareStatistic = chiSq,
            pValue = pVal,
            conformsToBenford = pVal > significanceLevel
        )
         */
    }

}
