package cc.jang.callmonitor.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.CallSuper

/**
 * Workaround for using hilt with services.
 * Exposes [onReceive] implementation required to be called in a subclass for injecting dependencies by Hilt.
 */
abstract class HiltBroadcastReceiver : BroadcastReceiver() {
    @CallSuper
    override fun onReceive(context: Context, intent: Intent) = Unit
}
