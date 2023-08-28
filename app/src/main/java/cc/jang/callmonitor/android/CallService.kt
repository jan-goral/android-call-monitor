package cc.jang.callmonitor.android

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.ktor.callServer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class CallService : Service() {

    @Inject
    lateinit var callApi: Call.Api

    @Inject
    lateinit var state: State

    private val server by lazy { callServer(callApi) }

    override fun onCreate() {
        super.onCreate()
        state.update { Call.Server.Status.Syncing() }
        server.start()
        state.update { Call.Server.Status.Started() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent) = null

    override fun onDestroy() {
        Thread {
            state.update { Call.Server.Status.Syncing() }
            server.stop(1000, 1000)
            state.update { Call.Server.Status.Stopped() }
        }.start()
    }


    @Singleton
    class State @Inject constructor() : Call.Server.State,
        MutableStateFlow<Call.Server.Status> by MutableStateFlow(Call.Server.Status.Stopped())
}

fun Context.startCallService() {
    val intent = Intent(this, CallService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

fun Context.stopCallService() {
    val intent = Intent(this, CallService::class.java)
    stopService(intent)
}
