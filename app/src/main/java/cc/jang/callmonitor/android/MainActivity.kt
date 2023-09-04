package cc.jang.callmonitor.android

import android.Manifest.permission.POST_NOTIFICATIONS
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.compose.call.CallScreen
import cc.jang.callmonitor.compose.theme.CallMonitorTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionsStore: PermissionsStore

    @Inject
    lateinit var toggleServer: Call.Server.Toggle

    private val askForPermissions = registerForActivityResult(
        RequestMultiplePermissions()
    ) { result ->
        if (SDK_INT >= TIRAMISU && result[POST_NOTIFICATIONS] == true) {
            try {
                toggleServer(true)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        permissionsStore.refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askForPermissions.launch(permissionsStore.missing.toTypedArray())
        try {
            toggleServer(true)
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

