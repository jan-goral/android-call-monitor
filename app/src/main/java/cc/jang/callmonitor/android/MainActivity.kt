package cc.jang.callmonitor.android

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import cc.jang.callmonitor.ui.screen.CallScreen
import cc.jang.callmonitor.ui.theme.CallMonitorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val askPermissions = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        try {
            startCallService()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            PackageManager.PERMISSION_GRANTED != checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
        ) {
            askPermissions.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

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
