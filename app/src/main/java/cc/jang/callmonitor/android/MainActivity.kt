package cc.jang.callmonitor.android

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_CALL_LOG
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import cc.jang.callmonitor.ui.screen.CallScreen
import cc.jang.callmonitor.ui.theme.CallMonitorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    @Named("permissionsBroadcast")
    lateinit var permissionsBroadcast: MutableSharedFlow<String>

    private val askForPermissions = registerForActivityResult(
        RequestMultiplePermissions()
    ) { result ->
        if (SDK_INT >= TIRAMISU && result[POST_NOTIFICATIONS] == true) {
            try {
                startCallService()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        if (result[READ_CALL_LOG] == true) {
            permissionsBroadcast.tryEmit(READ_CALL_LOG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askForPermissions.launch(
            buildList {
                if (SDK_INT >= TIRAMISU) add(POST_NOTIFICATIONS)
                add(READ_CALL_LOG)
            }.toTypedArray()
        )
        try {
            startCallService()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        setContent {
            CallMonitorTheme {
                CallScreen()
            }
        }
    }
}
