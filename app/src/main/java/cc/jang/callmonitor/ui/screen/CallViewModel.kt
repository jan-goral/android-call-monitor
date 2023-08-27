package cc.jang.callmonitor.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.Ip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    ipRepo: Ip.Repository,
    private val callApi: Call.Api,
) : ViewModel() {

    val status = callApi.state

    val address = ipRepo.ip
        .map { ip -> ip + ":" + callApi.config.port }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val calls = callApi.log
}
