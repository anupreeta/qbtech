package com.example.benford.api

import com.example.benford.application.BenfordService
import com.example.benford.models.BenfordRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.benfordRoutes(service: BenfordService) {
    post("/benford/analyze") {
        val request = call.receive<BenfordRequest>()
        val response = service.analyze(request.input, request.significanceLevel)
        call.respond(HttpStatusCode.OK, response)
    }
}