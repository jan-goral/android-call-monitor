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
class CallHandler @Inject constructor(
    override val config: Call.Api.Config,
    private val state: Call.Server.State,
    private val callRepo: Call.Repository,
    ipRepo: Ip.Repository,
) : Call.Api,
    Call.Repository by callRepo,
    CoroutineScope {

    override val coroutineContext = SupervisorJob()

    override val address: StateFlow<URI> = ipRepo.ip
        .map { ip -> ip.uri }
        .stateIn(this, SharingStarted.Eagerly, ipRepo.ip.value.uri)

    override fun getMetadata(): Call.Api.Metadata {
        val address = address.value
        val start = state.value.takeIf { it is Call.Server.Status.Started }?.date
        return Call.Api.Metadata(
            start = start,
            services = listOf("status", "log").map {
                Call.Api.Service(
                    name = it,
                    uri = address.resolve("/$it"),
                )
            }
        )
    }

    private val String.uri get() = URI.create("http://" + this + ":" + config.port)
}
