package com.example.benford.api

import com.example.benford.testUtil.SampleDataGenerator.generateBenfordNumbersWithDecimals
import com.example.benford.models.BenfordRequest
import com.example.benford.models.BenfordResponse
import com.example.benford.module
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

private const val ENDPOINT = "/benford/analyze"

private const val SIGNIFICANCE_LEVEL = 0.05

class BenfordApiTest {
    @Test
    fun `should conform benfords law with sample data`() = testApplication {
        application { module() }
        val input =
            "In 1,253 AD, a small clinic in the village of Greystone served 1,024 residents. By 1,587, they had developed primitive diagnostic tools, logging 1,950 test subjects in a single year. The population of the kingdom rose to 2,345,000 by 2,100 CE, according to census records.\n" +
                    "Dr. Althea’s old journal noted 1.4 mmol/L of potassium in a young patient, while another had 2.3 mmol/L. A case of hyperglycemia showed blood glucose at 13.6 mmol/L. The average cholesterol level in a study of 1,450 patients was 5.2 mmol/L, with LDL levels peaking at 7.3 mmol/L in 312 of them.\n" +
                    "A critical patient’s liver enzymes were AST: 190.4 U/L and ALT: 170.2 U/L, while bilirubin levels rose to 3.8 mg/dL. The same report listed creatinine at 1.09 mg/dL and a glomerular filtration rate (GFR) of 89.5 mL/min/1.73m².\n" +
                    "Another study reported 3.2 billion erythrocytes per mL in a rare blood disorder. In contrast, a healthy subject had 4.8 million red blood cells per microliter and hemoglobin at 13.2 g/dL. Platelet counts ranged from 150,000 to 390,000 per microliter, with anomalies detected in 2.7% of the participants."
        val payload = BenfordRequest(input, SIGNIFICANCE_LEVEL)
        val response = client.post(ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(payload))
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val result = Json.decodeFromString<BenfordResponse>(response.bodyAsText())
        assertEquals(9, result.expectedDistribution.size)
        assertEquals(32, result.actualDistribution.values.sum())
        validateExpectedDistribution(result)
        // Assert actualDistribution contains counts
        assertTrue(result.actualDistribution.isNotEmpty())
        assertTrue(
            result.actualDistribution.values.containsAll(
                listOf(14, 6, 5, 2, 2, 0, 1, 1, 1)
            )
        )
        assertEquals(5.689856, result.chiSquareStatistic)
        assertEquals(0.681927, result.pValue)


        assertTrue(result.conformsToBenford)
        assertTrue(result.chiSquareStatistic >= 0.0)
        assertTrue(result.pValue in 0.0..1.0)
    }

    @ParameterizedTest
    @ValueSource(ints = [350, 1000, 5000, 8000, 10000, 20000, 80000])
    fun `should analyze datasets of varied lengths correctly`(number: Int) = testApplication {
        application { module() }
        val input = generateBenfordNumbersWithDecimals(number)
        val payload = BenfordRequest(input, SIGNIFICANCE_LEVEL)
        val response = client.post(ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(payload))
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val result = Json.decodeFromString<BenfordResponse>(response.bodyAsText())

        assertTrue(result.actualDistribution.isNotEmpty())
        assertTrue(result.actualDistribution.values.sum() > 0)

        assertEquals(9, result.expectedDistribution.size)
        validateExpectedDistribution(result)

        assertTrue(result.chiSquareStatistic >= 0.0)
        assertTrue(result.pValue in 0.0..1.0)

        val expectedConformance = result.pValue > 0.05
        assertEquals(expectedConformance, result.conformsToBenford)
    }

    @Test
    fun `should analyze random numbers correctly that conform benford's law`() = testApplication {
        application { module() }
        val input =
            "27572.49, 207451.34, 238280.63, 116162.43, 605784.05, 63569.8, 202971.68, 282924.79, 71390.22, 145243.08,\n" +
                    "664834.23, 119487.55, 929378.79, 158520.89, 156078.06, 311489.33, 253115.36, 2862.35, 185802.91, 281633.78,\n" +
                    "47862.91, 102395.15, 32791.82, 84315.44, 514267.88, 14563.71, 38877.64, 96234.55, 79325.37, 121897.16,\n" +
                    "30249.06, 71983.29, 346971.45, 25813.99, 143849.5, 153672.68, 163423.94, 284577.89, 19632.06, 116293.74,\n" +
                    "41627.53, 128672.85, 399847.3, 27514.31, 18235.76, 54862.14, 87092.31, 18864.29, 13072.53, 122391.67\n"
        val payload = BenfordRequest(input, SIGNIFICANCE_LEVEL)

        val response = client.post(ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(payload))
        }

        val result = Json.decodeFromString<BenfordResponse>(response.bodyAsText())
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(9, result.expectedDistribution.size)
        val body = response.bodyAsText()
        println("Response Test 2: $body")
        assertTrue(body.contains("actualDistribution"))
    }

    @Test
    fun `should fail to conform Benford's law for fixed salaries data`() = testApplication {
        application { module() }
        val input = "30000, 32000, 35000, 37000, 40000, 40000, 40000, 42000, 42000, 45000,\n" +
                "45000, 45000, 47000, 47000, 50000, 50000, 50000, 52000, 52000, 54000,\n" +
                "55000, 55000, 55000, 58000, 58000, 60000, 60000, 62000, 65000, 67000,\n" +
                "70000, 70000, 70000, 72000, 72000, 75000, 77000, 80000, 80000, 80000,\n" +
                "82000, 85000, 88000, 90000, 90000, 92000, 95000, 97000, 100000, 105000\n"
        val payload = BenfordRequest(input, SIGNIFICANCE_LEVEL)

        val response = client.post(ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(payload))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val result = Json.decodeFromString<BenfordResponse>(response.bodyAsText())

        assertTrue(result.actualDistribution.isNotEmpty())
        assertTrue(result.actualDistribution.values.sum() > 0)
        validateExpectedDistribution(result)
        assertEquals(9, result.expectedDistribution.size)
        assertTrue(result.chiSquareStatistic >= 0.0)
        assertFalse(result.conformsToBenford)
        assertTrue(result.pValue in 0.0..1.0)
    }

    @Test
    fun `should fail analyse for empty input`() = testApplication {
        application { module() }
        val payload = BenfordRequest(" ", SIGNIFICANCE_LEVEL)

        val response = client.post(ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(payload))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `should fail analyze for invalid significance level`() = testApplication {
        application { module() }
        val payload = BenfordRequest(" ", 2.0)

        val response = client.post(ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(payload))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    private fun validateExpectedDistribution(result: BenfordResponse) {
        assertEquals(BigDecimal("0.301030"), result.expectedDistribution[1])
        assertEquals(BigDecimal("0.176091"), result.expectedDistribution[2])
        assertEquals(BigDecimal("0.124939"), result.expectedDistribution[3])
        assertEquals(BigDecimal("0.096910"), result.expectedDistribution[4])
        assertEquals(BigDecimal("0.079181"), result.expectedDistribution[5])
        assertEquals(BigDecimal("0.066947"), result.expectedDistribution[6])
        assertEquals(BigDecimal("0.057992"), result.expectedDistribution[7])
        assertEquals(BigDecimal("0.051153"), result.expectedDistribution[8])
        assertEquals(BigDecimal("0.045757"), result.expectedDistribution[9])
    }
}

