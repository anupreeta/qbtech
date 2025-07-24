package benford.routes

import benford.api.benfordRoutes
import benford.exceptions.BenfordException
import benford.models.ErrorResponse
import benford.application.BenfordService
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
        exception<BenfordException.InvalidInputException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid Input", cause.message))
        }

        exception<BenfordException.InvalidSignificanceLevelException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid Significance Level", cause.message))
        }

        exception<BenfordException.InsufficientDataException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Insufficient Data", cause.message))
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
