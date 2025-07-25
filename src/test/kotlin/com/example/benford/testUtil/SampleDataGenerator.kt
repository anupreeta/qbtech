package com.example.benford.testUtil

import kotlin.math.log10
import kotlin.random.Random

object SampleDataGenerator {
    fun generateBenfordNumbersWithDecimals(count: Int = 500): String {
        val random = Random(42) // use a seed for reproducibility
        val labels = listOf("Glucose", "ALT", "Cholesterol", "WBC Count", "Heart Rate", "Creatinine")

        // Benford probabilities for digits 1 to 9
        val benfordProbs = (1..9).map { d -> log10(1.0 + 1.0 / d) }
        val cumulativeProbs = benfordProbs.runningReduce { acc, d -> acc + d }

        fun generateFirstDigit(): Int {
            val r = random.nextDouble()
            for ((index, cp) in cumulativeProbs.withIndex()) {
                if (r <= cp) return index + 1
            }
            return 9
        }

        fun generateNumber(): String {
            val firstDigit = generateFirstDigit()
            val additionalDigitsCount = random.nextInt(1, 6) // 1 to 5 digits
            val additionalDigits = (1..additionalDigitsCount)
                .map { random.nextInt(0, 10) }
                .joinToString("")

            var numberStr = "$firstDigit$additionalDigits"

            // Randomly insert decimal
            if (random.nextBoolean()) {
                val decimalPos = random.nextInt(1, numberStr.length)
                numberStr = numberStr.substring(0, decimalPos) + "." + numberStr.substring(decimalPos)
            }

            return numberStr
        }

        return List(count) {
            val label = labels.random(random)
            val value = generateNumber()
            "$label: $value"
        }.shuffled(random).joinToString(", ")
    }
}
