package com.qbtech.com.qtest.routes

import com.qbtech.com.qtest.controller.benfordRoutes
import com.qbtech.com.qtest.service.BenfordService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }

    val service = BenfordService()

    routing {
        get("/") {
            call.respondText("Benford API is live", status = HttpStatusCode.OK)
        }
        benfordRoutes(service)
    }
}
