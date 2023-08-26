package cc.jang.callmonitor

object Ip {
    interface Repository {
        fun getLocalIp(): String
    }
}
