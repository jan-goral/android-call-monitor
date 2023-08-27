package cc.jang.callmonitor.android

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.ktor.callServer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.update

@AndroidEntryPoint
class CallService : Service() {

    @Inject
    lateinit var callApi: Call.Api

    @Inject
    lateinit var callApiState: Call.Api.State

    private val server by lazy { callServer(callApi) }

    override fun onCreate() {
        super.onCreate()
        callApiState.update { Call.Api.Status.Syncing }
        server.start()
        callApiState.update { Call.Api.Status.Started }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent) = null

    override fun onDestroy() {
        Thread {
            callApiState.update { Call.Api.Status.Syncing }
            server.stop(1000, 1000)
            callApiState.update { Call.Api.Status.Stopped }
        }.start()
    }
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
