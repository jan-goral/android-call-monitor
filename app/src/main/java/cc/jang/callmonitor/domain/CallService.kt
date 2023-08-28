package cc.jang.callmonitor.domain

import cc.jang.callmonitor.Call
import cc.jang.callmonitor.Ip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallService @Inject constructor(
    override val config: Call.Service.Config,
    private val state: Call.Server.State,
    private val callRepo: Call.Repository,
    ipRepo: Ip.Repository,
) : Call.Service,
    Call.Repository by callRepo,
    CoroutineScope {

    override val coroutineContext = SupervisorJob()

    override val address: StateFlow<URI> = ipRepo.ip
        .map { ip -> ip.uri }
        .stateIn(this, SharingStarted.Eagerly, ipRepo.ip.value.uri)

    override val metadata: Call.Service.Metadata
        get() {
            val start = state.value.takeIf { it is Call.Server.Status.Started }?.date
            val address = address.value
            val services = listOf("status", "log").map { name ->
                Call.Service.Endpoint(
                    name = name,
                    uri = address.resolve("/$name"),
                )
            }
            return Call.Service.Metadata(
                start = start,
                services = services
            )
        }

    private val String.uri get() = URI.create("http://" + this + ":" + config.port)
}
