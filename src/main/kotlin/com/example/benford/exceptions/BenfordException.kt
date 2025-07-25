package com.example.benford.exceptions

sealed class BenfordException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
    class ChiSquareComputationException(message: String, cause: Throwable? = null) :
        BenfordException(message, cause)
}