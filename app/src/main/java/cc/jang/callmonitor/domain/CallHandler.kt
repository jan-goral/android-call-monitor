package cc.jang.callmonitor.domain

import cc.jang.callmonitor.Call
import cc.jang.callmonitor.Ip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.net.URI
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallHandler @Inject constructor(
    start: Date = Date(),
    override val config: Call.Api.Config,
    private val callRepo: Call.Repository,
    private val ipRepo: Ip.Repository,
) : Call.Api,
    Call.Repository by callRepo,
    CoroutineScope {

    override val coroutineContext = SupervisorJob()

    private val scope = CoroutineScope(SupervisorJob())

    override val state = State()

    private val started = state
        .filter { it == Call.Api.Status.Started }
        .map { Date() }
        .stateIn(scope, SharingStarted.Eagerly, start)

    override fun getMetadata(): Call.Api.Metadata {
        val address = getAddress()
        return Call.Api.Metadata(
            start = started.value,
            services = listOf("status", "log").map {
                Call.Api.Service(
                    name = it,
                    uri = URI.create("$address/$it")
                )
            }
        )
    }

    private fun getAddress() = ipRepo.ip.value + ":" + config.port

    class State : Call.Api.State,
        MutableStateFlow<Call.Api.Status> by MutableStateFlow(Call.Api.Status.Stopped)
}
