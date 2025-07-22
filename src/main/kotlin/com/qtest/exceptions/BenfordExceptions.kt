package com.qtest.exceptions

class BenfordExceptions {
    class InvalidInputException(message: String) : RuntimeException(message)
    class InvalidSignificanceLevelException(message: String) : RuntimeException(message)
    class InsufficientDataException(message: String) : RuntimeException(message)
}