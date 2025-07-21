package com.qbtech

import com.qbtech.com.qtest.models.BenfordRequest
import com.qbtech.com.qtest.models.BenfordResponse
import com.qbtech.com.qtest.module
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
//import junit.framework.TestCase.assertTrue
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

private const val ENDPOINT = "/benford/analyze"

private const val SIGNIFICANCE_LEVEL = 0.05

class BenfordApiTest {
    @Test
    fun testBenfordApiConformancePopulationData() = testApplication {
        application { module() }
        val input = "In 1,253 AD, a small clinic in the village of Greystone served 1,024 residents. By 1,587, they had developed primitive diagnostic tools, logging 1,950 test subjects in a single year. The population of the kingdom rose to 2,345,000 by 2,100 CE, according to census records.\n" +
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

        // ✅ Assert actualDistribution contains counts
        assertTrue(result.actualDistribution.isNotEmpty())
        assertTrue(result.actualDistribution.values.sum() > 0)

        // ✅ Assert expectedDistribution is present and valid
        assertEquals(9, result.expectedDistribution.size)
        assertEquals(BigDecimal("30.10"), result.expectedDistribution[1])
        assertEquals(BigDecimal("17.61"), result.expectedDistribution[2])
        assertEquals(BigDecimal("4.58"), result.expectedDistribution[9])

        assertEquals(expectedBenfordDistribution, result.expectedDistribution)

        // ✅ Assert chi-square test values are present
        assertTrue(result.chiSquareStatistic >= 0.0)
        assertTrue(result.pValue in 0.0..1.0)

        // ✅ Assert that the test conforms (or not) to Benford depending on p-value
        val expectedConformance = result.pValue > 0.05
        assertEquals(expectedConformance, result.conformsToBenford)
        val body = response.bodyAsText()
        println("Response Test 1: $body")
    }

    @Test
    fun testBenfordApiConformanceRandomNumbers() = testApplication {
        application { module() }
        val input = "27572.49, 207451.34, 238280.63, 116162.43, 605784.05, 63569.8, 202971.68, 282924.79, 71390.22, 145243.08,\n" +
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
        assertEquals(expectedBenfordDistribution, result.expectedDistribution)
        val body = response.bodyAsText()
        println("Response Test 2: $body")
        assertTrue(body.contains("actualDistribution"))
    }

    @Test
    fun testBenfordApiFailsForFixedSalaries() = testApplication {
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

        // ✅ Assert actualDistribution contains counts
        assertTrue(result.actualDistribution.isNotEmpty())
        assertTrue(result.actualDistribution.values.sum() > 0)

        // ✅ Assert expectedDistribution is present and valid
        assertEquals(9, result.expectedDistribution.size)
        assertEquals(expectedBenfordDistribution, result.expectedDistribution)
        assertTrue(result.chiSquareStatistic >= 0.0)
        assertFalse(result.conformsToBenford)
        assertTrue(result.pValue in 0.0..1.0)
    }

    companion object {
        val expectedBenfordDistribution = mapOf(
            1 to BigDecimal("30.10"),
            2 to BigDecimal("17.61"),
            3 to BigDecimal("12.49"),
            4 to BigDecimal("9.69"),
            5 to BigDecimal("7.92"),
            6 to BigDecimal("6.69"),
            7 to BigDecimal("5.80"),
            8 to BigDecimal("5.12"),
            9 to BigDecimal("4.58")
        )
    }


}