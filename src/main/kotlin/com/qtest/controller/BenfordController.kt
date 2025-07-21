package com.qbtech.com.qtest.controller

import com.qbtech.com.qtest.models.BenfordRequest
import com.qbtech.com.qtest.service.BenfordService
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