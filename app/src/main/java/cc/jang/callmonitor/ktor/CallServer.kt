package cc.jang.callmonitor.ktor

import cc.jang.callmonitor.Call
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import javax.inject.Inject

class CallServer @Inject constructor(
    service: Call.Service,
) : ApplicationEngine by callServer(service)

fun callServer(
    service: Call.Service,
) = embeddedServer(Netty, service.config.port) {
    callModule(service)
}

fun Application.callModule(
    service: Call.Service,
) {
    install(ContentNegotiation) {
        jackson {
            dateFormat = service.config.dateFormat
        }
    }
    routing {
        get("/") {
            call.respond(service.metadata)
        }
        get("/status") {
            call.respond(service.status ?: Unit)
        }
        get("/log") {
            call.respond(service.log.value)
        }
    }
}
