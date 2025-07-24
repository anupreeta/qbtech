package benford.exceptions

sealed class BenfordException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
    class InvalidInputException(message: String) : BenfordException(message)
    class InvalidSignificanceLevelException(message: String) : BenfordException(message)
    class InsufficientDataException(message: String) : BenfordException(message)
    class ChiSquareComputationException(message: String, cause: Throwable? = null) :
        BenfordException(message, cause)
}