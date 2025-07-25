package com.example.benford.infrastructure

import com.example.benford.exceptions.BenfordException
import org.apache.commons.math3.stat.inference.ChiSquareTest
import java.math.BigDecimal
import java.math.RoundingMode

object ChiSquareEvaluator {
    fun evaluate(expected: DoubleArray, observed: LongArray): Pair<Double, Double> {
        require(expected.size == observed.size) {"Expected and observed arrays must be of the same length."}
        require(expected.all { it > 0.0 }) {"Expected frequencies must all be greater than zero."}

        return try {
            val test = ChiSquareTest()
            val rawStatistic = test.chiSquare(expected, observed)
            val rawPValue = test.chiSquareTest(expected, observed)

            val roundedStatistic = BigDecimal(rawStatistic).setScale(6, RoundingMode.HALF_UP).toDouble()
            val roundedPValue = BigDecimal(rawPValue).setScale(6, RoundingMode.HALF_UP).toDouble()

            roundedStatistic to roundedPValue
        } catch (e: IllegalArgumentException) {
            throw BenfordException.ChiSquareComputationException("Chi-Square test failed due to invalid input: ${e.message}", e)
        } catch (e: Exception) {
            throw BenfordException.ChiSquareComputationException("Unexpected error during Chi-Square evaluation", e)
        }
    }
}