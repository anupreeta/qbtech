package com.example.benford.routes

import com.example.benford.api.benfordRoutes
import com.example.benford.models.ErrorResponse
import com.example.benford.application.BenfordService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Server Error", "Unexpected server error"))
        }
    }

    val service = BenfordService()

    routing {
        get("/") {
            call.respondText("Benford API is live", status = HttpStatusCode.OK)
        }
        benfordRoutes(service)
    }
}
