package cc.jang.callmonitor.domain

import cc.jang.callmonitor.Call
import cc.jang.callmonitor.Ip
import java.net.URI
import java.util.Date

class CallHandler(
    override val config: Call.Api.Config,
    private val start: Date = Date(),
    private val callRepo: Call.Repository,
    private val ipRepo: Ip.Repository,
) : Call.Api,
    Call.Repository by callRepo {

    override fun getMetadata(): Call.Api.Metadata {
        val address = getAddress()
        return Call.Api.Metadata(
            start = start,
            services = listOf("status", "log").map {
                Call.Api.Service(
                    name = it,
                    uri =  URI.create("$address/$it")
                )
            }
        )
    }

    private fun getAddress() = ipRepo.ip.value + ":" + config.port
}
