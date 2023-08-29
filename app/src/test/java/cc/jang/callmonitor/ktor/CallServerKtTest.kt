package cc.jang.callmonitor.ktor

import cc.jang.callmonitor.Call
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CallServerKtTest {

    private lateinit var service: Call.Service

    @BeforeTest
    fun setUp() {
        service = mockk {
            every { config } returns Call.Service.Config(
                port = 0,
                dateFormat = SimpleDateFormat().apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            )
        }
    }

    @Test
    fun testRoot() = testApplication {
        // given
        application { callModule(service) }
        every { service.metadata } returns Call.Service.Metadata(
            start = Date(0),
            services = listOf(
                Call.Service.Endpoint(uri = URI("https://0.0.0.0:8080/test"))
            )
        )

        // when
        val response = client.get("/")

        // then
        verify(exactly = 1) { service.metadata }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            """{"start":"01.01.1970, 00:00","services":[{"name":"","uri":"https://0.0.0.0:8080/test"}]}""",
            response.bodyAsText(),
        )
    }

    @Test
    fun testStatus() = testApplication {
        // given
        application { callModule(service) }
        every { service.status } returns Call.Status()

        // when
        val response = client.get("status")

        // then
        verify(exactly = 1) { service.status }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            """{{"time":"01.01.1970, 00:00","outgoing":true,"ongoing":true,"number":"","name":null}""",
            response.bodyAsText(),
        )
    }

    @Test
    fun testStatusEmpty() = testApplication {
        // given
        application { callModule(service) }
        every { service.status } returns null

        // when
        val response = client.get("status")

        // then
        verify(exactly = 1) { service.status }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            """{}""",
            response.bodyAsText(),
        )
    }

    @Test
    fun testLog() = testApplication {
        // given
        application { callModule(service) }
        every { service.log.value } returns listOf(Call.Log())

        // when
        val response = client.get("/log")

        // then
        verify(exactly = 1) { service.log.value }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            """[{"beginning":"01.01.1970, 00:00","duration":0,"number":"","name":null,"timesQueried":0}]""",
            response.bodyAsText(),
        )
    }
}
