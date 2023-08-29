package cc.jang.callmonitor

import kotlinx.coroutines.flow.StateFlow

/**
 * The core abstraction of the local network IP address.
 */
object Ip {
    interface Repository {
        val ip: StateFlow<String>
    }
}
