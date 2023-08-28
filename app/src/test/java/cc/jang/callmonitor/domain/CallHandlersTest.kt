package cc.jang.callmonitor.domain

import cc.jang.callmonitor.Call
import cc.jang.callmonitor.Ip
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Test
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class CallHandlersTest {

    private val start = Date(0)
    private val port = 0
    private val config = Call.Api.Config(port, SimpleDateFormat.getInstance())
    private lateinit var callHandler: CallHandler
    @MockK
    private lateinit var callRepo: Call.Repository
    @MockK
    private lateinit var ipRepo: Ip.Repository
    @MockK
    private lateinit var callServerState: Call.Server.State
    @MockK
    private lateinit var startedStatus: Call.Server.Status.Started

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { ipRepo.ip.value } returns "ip"
        every { startedStatus.date } returns start
        every { callServerState.value } returns startedStatus
        callHandler = CallHandler(
            config = config,
            state = callServerState,
            callRepo = callRepo,
            ipRepo = ipRepo
        )
    }

    @Test
    fun getMetadata() {
        // given
        val expected = Call.Api.Metadata(
            start = start,
            services = listOf("status", "log").map { name ->
                Call.Api.Service(name, URI("http://ip:${config.port}/$name"))
            }
        )

        // when
        val actual = callHandler.getMetadata()

        // then
        assertEquals(expected, actual)
    }
}
