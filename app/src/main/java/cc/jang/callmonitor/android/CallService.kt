package cc.jang.callmonitor.android

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.domain.CallHandler
import cc.jang.callmonitor.ktor.callServer
import cc.jang.callmonitor.mock.CallRepository
import cc.jang.callmonitor.mock.IpRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.text.DateFormat

class CallService : Service(), CoroutineScope {

    override val coroutineContext = SupervisorJob() + Dispatchers.IO

    private val server by lazy {
        callServer(
            CallHandler(
                config = Call.Api.Config(
                    port = 12345,
                    dateFormat = DateFormat.getDateTimeInstance(),
                ),
                callRepo = CallRepository(),
                ipRepo = IpRepository(),
            )
        )
    }

    override fun onCreate() {
        server.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent) = null

    override fun onDestroy() {
        server.stop(3000, 3000)
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
