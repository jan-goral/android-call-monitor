package cc.jang.callmonitor.ui.screen

import androidx.lifecycle.ViewModel
import cc.jang.callmonitor.Call
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    callApi: Call.Api,
    val status: Call.Server.State,
) : ViewModel() {

    val address = callApi.address

    val calls = callApi.log
}
