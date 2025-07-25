package com.example.benford.models

import com.example.benford.serialization.BigDecimalMapSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class BenfordResponse(
    val actualDistribution: Map<Int, Long>,
    @Serializable(with = BigDecimalMapSerializer::class)
    val expectedDistribution: Map<Int, BigDecimal>,
    val chiSquareStatistic: Double,
    val pValue: Double,
    val conformsToBenford: Boolean
)
