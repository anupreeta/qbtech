package com.qbtech.com.qtest.models

import kotlinx.serialization.Serializable

@Serializable
data class BenfordRequest(val input: String, val significanceLevel: Double)
