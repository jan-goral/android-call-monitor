package cc.jang.callmonitor.mock

import cc.jang.callmonitor.Ip
import kotlinx.coroutines.flow.MutableStateFlow

class IpRepository : Ip.Repository {
    override val ip = MutableStateFlow("http://0.0.0.0")
}
