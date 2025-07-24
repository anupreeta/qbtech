package benford.application

import benford.api.BenfordApiTest.Companion.expectedBenfordDistribution
import benford.benford.testUtil.SampleDataGenerator.generateBenfordNumbersWithDecimals
import benford.exceptions.BenfordException
import benford.models.BenfordResponse
import org.junit.jupiter.api.Assertions
import java.math.BigDecimal
import kotlin.test.*


private const val SIGNIFICANCE_LEVEL = 0.05

class BenfordServiceTest {
    private val service = BenfordService()

    @Test
    fun `analyze should return expected result for realistic data for dataset size of 350`() {
        val input = generateBenfordNumbersWithDecimals(350)

        val result = service.analyze(input, significanceLevel = SIGNIFICANCE_LEVEL)

        assertEquals(9, result.expectedDistribution.size)
        assertEquals(expectedBenfordDistribution, result.expectedDistribution)
        validateExpectedDistribution(result)
        println(input)
        Assertions.assertTrue(result.actualDistribution.isNotEmpty())
        assertEquals(350, result.actualDistribution.values.sum())
        assertEquals(8.426284, result.chiSquareStatistic)
        assertEquals( 0.392974, result.pValue)
        assertTrue(result.conformsToBenford)
        assertTrue(result.pValue > SIGNIFICANCE_LEVEL)

        assertTrue(result.actualDistribution.values.containsAll(listOf(112, 74, 34, 31, 28, 15, 21, 19, 16)))
    }

    @Test
    fun `analyze should return expected result for realistic data for dataset size of 1000`() {
        val input = generateBenfordNumbersWithDecimals(1000)

        val result = service.analyze(input, significanceLevel = SIGNIFICANCE_LEVEL)

        assertEquals(9, result.expectedDistribution.size)
        assertEquals(expectedBenfordDistribution, result.expectedDistribution)
        validateExpectedDistribution(result)
        Assertions.assertTrue(result.actualDistribution.isNotEmpty())
        assertTrue(result.actualDistribution.values.containsAll(
            listOf(317, 183, 121, 88, 85, 54, 60, 46, 46)))

        assertEquals(1000,result.actualDistribution.values.sum())
        assertEquals(5.570229, result.chiSquareStatistic)
        assertEquals( 0.695247, result.pValue)
        assertTrue(result.conformsToBenford)
        assertTrue(result.pValue > SIGNIFICANCE_LEVEL)
    }

    @Test
    fun `analyze should return expected result for realistic data for dataset size of 10000`() {
        val input = generateBenfordNumbersWithDecimals(10000)

        val result = service.analyze(input, significanceLevel = SIGNIFICANCE_LEVEL)

        assertEquals(9, result.expectedDistribution.size)
        assertEquals(expectedBenfordDistribution, result.expectedDistribution)
        validateExpectedDistribution(result)
        Assertions.assertTrue(result.actualDistribution.isNotEmpty())
        assertTrue(result.actualDistribution.values.containsAll(listOf(3060, 1808, 1263, 943, 772, 628, 565, 479, 482)))

        assertEquals(10000,result.actualDistribution.values.sum())
        assertEquals(9.7298, result.chiSquareStatistic)
        assertEquals( 0.284505, result.pValue)
        assertTrue(result.conformsToBenford)
        assertTrue(result.pValue > SIGNIFICANCE_LEVEL)
    }

    @Test
    fun `analyze should return expected result for realistic data for dataset size of 80000`() {
        val input = generateBenfordNumbersWithDecimals(80000)

        val result = service.analyze(input, significanceLevel = SIGNIFICANCE_LEVEL)

        assertEquals(9, result.expectedDistribution.size)
        assertEquals(expectedBenfordDistribution, result.expectedDistribution)
        validateExpectedDistribution(result)
        Assertions.assertTrue(result.actualDistribution.isNotEmpty())

        println(result.chiSquareStatistic)
        println(result.pValue)
        println(result.actualDistribution.values)
        assertEquals(80000,result.actualDistribution.values.sum())
        assertEquals(5.945052, result.chiSquareStatistic)
        assertEquals( 0.653387, result.pValue)
        assertTrue(result.actualDistribution.values.containsAll(
            listOf(24168, 14083, 10064, 7800, 6322, 5273, 4610, 3983, 3697)))

        assertTrue(result.conformsToBenford)
        assertTrue(result.pValue > SIGNIFICANCE_LEVEL)
    }


    @Test
    fun `analyze should return expected result for realistic data`() {
        val input = """
            123, 1567, 1900, 1450, 178, 10200, 113, 1040, 19800, 167,
            2300, 280, 290, 210, 250, 270, 223, 239, 265,
            330, 360, 390, 300, 310, 320, 345,
            450, 470, 410, 430, 490,
            520, 540, 580, 530,
            610, 650, 620, 690,
            730, 780,
            810, 860,
            920, 970
        """.trimIndent()

        val result = service.analyze(input, significanceLevel = SIGNIFICANCE_LEVEL)
        assertTrue(result.conformsToBenford)
        assertTrue(result.pValue > SIGNIFICANCE_LEVEL)
    }

    @Test
    fun `analyze should not conform to Benford for data having unevenly distributed leading digits`() {
        val input = (100..900 step 10).joinToString(", ")
        val result = service.analyze(input, significanceLevel = SIGNIFICANCE_LEVEL)
        assertFalse(result.conformsToBenford)
        assertTrue(result.pValue < SIGNIFICANCE_LEVEL)
    }

    @Test
    fun `analyze should conform to Benford given uniform distribution of leading digits and small sample size`() {
        val input = (100..900 step 100).joinToString(", ")
        val result = service.analyze(input, significanceLevel = SIGNIFICANCE_LEVEL)
        println("true test" + result)
        assertTrue(result.conformsToBenford)
        assertTrue(result.pValue > SIGNIFICANCE_LEVEL)
    }

    @Test
    fun analyze_shouldConformToBenford() {
        val input = listOf(
            123, 1450, 113, 19800, 167, 2300, 280,
            330, 360, 390, 450, 470, 520, 540, 610, 730, 810, 920
        ).joinToString(", ")
        val result = service.analyze(input, significanceLevel = SIGNIFICANCE_LEVEL)
        println("true test" + result)
        assertTrue(result.conformsToBenford)
        assertTrue(result.pValue > SIGNIFICANCE_LEVEL)
    }

    @Test
    fun `analyze should throw InsufficientDataException when input has less than 5 digits`() {
        val input = "100, 200, 300" // Only 3 leading digits
        val significanceLevel = SIGNIFICANCE_LEVEL

        val exception = assertFailsWith<BenfordException.InsufficientDataException> {
            service.analyze(input, significanceLevel)
        }

        assertEquals("Not enough numeric values to analyze", exception.message)
    }

    private fun validateExpectedDistribution(result: BenfordResponse) {
        assertEquals(BigDecimal("30.10"), result.expectedDistribution[1])
        assertEquals(BigDecimal("17.61"), result.expectedDistribution[2])
        assertEquals(BigDecimal("12.49"), result.expectedDistribution[3])
        assertEquals(BigDecimal("9.69"), result.expectedDistribution[4])
        assertEquals(BigDecimal("7.92"), result.expectedDistribution[5])
        assertEquals(BigDecimal("6.69"), result.expectedDistribution[6])
        assertEquals(BigDecimal("5.80"), result.expectedDistribution[7])
        assertEquals(BigDecimal("5.12"), result.expectedDistribution[8])
        assertEquals(BigDecimal("4.58"), result.expectedDistribution[9])
    }

}
