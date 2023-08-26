package cc.jang.callmonitor.android

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.ktor.callServer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallService : Service() {

    @Inject
    lateinit var callApi: Call.Api

    private val server by lazy { callServer(callApi) }

    override fun onCreate() {
        super.onCreate()
        server.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent) = null

    override fun onDestroy() {
        Thread {
            server.stop(3000, 3000)
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
