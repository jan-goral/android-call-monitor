package cc.jang.callmonitor.android

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.provider.CallLog
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class CallLogObserver @Inject constructor(
    private val contentResolver: ContentResolver,
    private val handler: Handler,
) {
    fun flow() = callbackFlow {
        val observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                channel.trySend(Unit)
            }
        }
        contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, observer)
        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }
    }
}
