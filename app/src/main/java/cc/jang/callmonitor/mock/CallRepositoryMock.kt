package cc.jang.callmonitor.mock

import cc.jang.callmonitor.Call
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepositoryMock @Inject constructor() : Call.Repository {
    override val status = Call.Ongoing()
    override val log = MutableStateFlow(listOf(Call.Previous(name = "test")))
}
