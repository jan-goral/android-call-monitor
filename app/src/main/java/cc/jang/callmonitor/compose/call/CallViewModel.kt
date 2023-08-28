package cc.jang.callmonitor.compose.call

import androidx.lifecycle.ViewModel
import cc.jang.callmonitor.Call
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    callService: Call.Service,
    val status: Call.Server.State,
) : ViewModel() {

    val address = callService.address

    val calls = callService.log
}
