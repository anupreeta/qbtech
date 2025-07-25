package com.example.benford.util

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.log10

private val numberRegex = Regex("""\b\d+(?:\.\d+)?\b""")

object BenfordAnalyzer {
    fun extractLeadingDigits(input: String): List<Int> {
        return numberRegex.findAll(input)
            .mapNotNull { match ->
                match.value.trimStart('0')
                    .firstOrNull { it.isDigit() && it != '0' }
                    ?.toString()?.toIntOrNull()
            }.toList()
    }

    fun calculateExpectedDistribution(): Map<Int, BigDecimal> =
        (1..9).associateWith {
            BigDecimal(log10(1 + 1.0 / it) * 100).setScale(2, RoundingMode.HALF_UP)
        }

    object BenfordExpectedDistribution {
        val distribution: Map<Int, BigDecimal> by lazy {
            val context = MathContext(10, RoundingMode.HALF_UP)
            (1..9).associateWith { digit ->
                BigDecimal(log10(1 + 1.0 / digit), context).setScale(6, RoundingMode.HALF_UP)
            }
        }
    }
}