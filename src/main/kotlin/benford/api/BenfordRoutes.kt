package benford.api

import benford.models.BenfordRequest
import benford.application.BenfordService
import benford.exceptions.BenfordException
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.benfordRoutes(service: BenfordService) {
    post("/benford/analyze") {
        val request = call.receive<BenfordRequest>()
        if (request.input.isBlank()) throw BenfordException.InvalidInputException("Input is empty or missing numbers")

        if (request.significanceLevel !in 0.0..1.0) {
            throw BenfordException.InvalidSignificanceLevelException("Significance level must be between 0 and 1")
        }

        val response = service.analyze(request.input, request.significanceLevel)
        call.respond(HttpStatusCode.OK, response)
    }
}