package cc.jang.callmonitor.mock

import cc.jang.callmonitor.Ip
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IpRepositoryMock @Inject constructor() : Ip.Repository {
    override val ip = MutableStateFlow("http://0.0.0.0")
}
