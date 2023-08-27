package cc.jang.callmonitor.ktor

import cc.jang.callmonitor.Call
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun callServer(
    api: Call.Api,
) = embeddedServer(Netty, api.config.port) {
    callModule(api)
}

fun Application.callModule(
    api: Call.Api,
) {
    install(ContentNegotiation) {
        jackson {
            dateFormat = api.config.dateFormat
        }
    }
    routing {
        get("/") {
            call.respond(api.getMetadata())
        }
        get("/status") {
            call.respond(api.status ?: Unit)
        }
        get("/log") {
            call.respond(api.log.value)
        }
    }
}
