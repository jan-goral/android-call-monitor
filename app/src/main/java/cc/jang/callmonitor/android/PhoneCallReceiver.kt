package cc.jang.callmonitor.android

import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import cc.jang.callmonitor.Call
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.update
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class PhoneCallReceiver : HiltBroadcastReceiver() {

    companion object {
        val Tag: String = PhoneCallReceiver::class.java.name
    }

    @Inject
    lateinit var callRepo: CallRepository

    @Inject
    lateinit var contactNameResolver: ContactNameResolver

    override fun onReceive(context: Context, intent: Intent) {
        logD(Tag, intent)

        intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED || return
        val extras = intent.extras ?: return
        // Unfortunately EXTRA_INCOMING_NUMBER is deprecated but other solutions require permissions like
        // android.permission.READ_PRECISE_PHONE_STATE or android.permission.BIND_SCREENING_SERVICE
        // available only for system apps.
        val number = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: return
        val state = extras.getString(TelephonyManager.EXTRA_STATE) ?: return

        super.onReceive(context, intent) // inject dependencies via Hilt

        callRepo.updateStatus { previous ->
            when {

                state == TelephonyManager.EXTRA_STATE_IDLE -> null

                // The case when a second call appears when the first one is ongoing.
                // The problem is that second call isn't broadcasts EXTRA_STATE_IDLE.
                // Observing on android.intent.action.PHONE_STATE doesn't allow to detect the second call finish.
                // Currently second call state is not supported.
                previous != null && previous.number != number -> previous

                state == TelephonyManager.EXTRA_STATE_RINGING -> when (previous) {
                    null -> Call.Status(
                        name = contactNameResolver.resolve(number),
                        number = number,
                        ongoing = false,
                        outgoing = false,
                        time = Date(),
                    )

                    else -> previous
                }

                state == TelephonyManager.EXTRA_STATE_OFFHOOK -> when (previous) {
                    null -> Call.Status(
                        name = contactNameResolver.resolve(number),
                        number = number,
                        ongoing = true,
                        outgoing = true,
                        time = Date(),
                    )

                    else -> previous.copy(
                        ongoing = true,
                        time = Date(),
                    )
                }

                else -> previous
            }.also { current ->
                val log = current ?: previous?.copy(time = Date()).let {  "$it - finish"}
                Log.d(Tag, "$log")
            }
        }
    }
}

private fun logD(tag: String, intent: Intent) {
    val data = intent.action to intent.extras?.run {
        keySet().associateWith { getString(it) }
    }
    Log.d(tag, data.toString())
}
