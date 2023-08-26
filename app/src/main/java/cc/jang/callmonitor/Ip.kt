package cc.jang.callmonitor

import kotlinx.coroutines.flow.StateFlow

object Ip {
    interface Repository {
        val ip: StateFlow<String?>
    }
}
