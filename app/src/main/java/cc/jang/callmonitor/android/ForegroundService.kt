package cc.jang.callmonitor.android

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.ktor.CallServer
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Responsible for starting, stopping, and keeping the HTTP service alive.
 */
@AndroidEntryPoint
class ForegroundService : Service() {

    @Inject
    lateinit var state: State

    @Inject
    lateinit var server: CallServer

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

    class Toggle @Inject constructor(
        @ApplicationContext private val context: Context
    ) : Call.Server.Toggle {
        override fun invoke(on: Boolean) = when {
            on -> context.startForegroundService()
            else -> context.stopForegroundService()
        }
    }
}

private fun Context.startForegroundService() {
    val intent = Intent(this, ForegroundService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

private fun Context.stopForegroundService() {
    val intent = Intent(this, ForegroundService::class.java)
    stopService(intent)
}
