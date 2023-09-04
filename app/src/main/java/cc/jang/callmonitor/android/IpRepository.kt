package cc.jang.callmonitor.android

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import cc.jang.callmonitor.Ip
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.Inet4Address
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IpRepository @Inject constructor(
    cm: ConnectivityManager,
) : Ip.Repository,
    ConnectivityManager.NetworkCallback() {

    private companion object {
        const val DefaultIP = "0.0.0.0"
        val networkRequest: NetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
    }

    override val ip = MutableStateFlow(DefaultIP)

    init {
        cm.registerNetworkCallback(networkRequest, this)
        val linkProperties = cm.getLinkProperties(cm.activeNetwork)
        ip.value = linkProperties?.inet4Address ?: DefaultIP
    }

    override fun onLost(network: Network) {
        ip.value = DefaultIP
    }

    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        ip.value = linkProperties.inet4Address ?: DefaultIP
    }

    private val LinkProperties.inet4Address
        get() = linkAddresses.map { linkAddress ->
            linkAddress.address
        }.find { inetAddress ->
            !inetAddress.isLoopbackAddress && inetAddress is Inet4Address
        }?.hostAddress
}
