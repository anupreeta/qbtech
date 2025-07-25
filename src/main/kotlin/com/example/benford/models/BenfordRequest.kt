package com.example.benford.models

import kotlinx.serialization.Serializable

@Serializable
data class BenfordRequest(val input: String, val significanceLevel: Double)
