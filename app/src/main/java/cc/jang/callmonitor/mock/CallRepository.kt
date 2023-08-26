package cc.jang.callmonitor.mock

import cc.jang.callmonitor.Call
import kotlinx.coroutines.flow.MutableStateFlow

class CallRepository : Call.Repository {
    override val status = MutableStateFlow(Call.Ongoing())
    override val log = MutableStateFlow(listOf(Call.Previous()))
}
