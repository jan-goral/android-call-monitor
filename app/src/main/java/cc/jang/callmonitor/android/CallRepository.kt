package cc.jang.callmonitor.android

import android.Manifest.permission.READ_CALL_LOG
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.database.ContentObserver
import android.os.Handler
import android.provider.CallLog
import android.provider.CallLog.Calls.CONTENT_URI
import cc.jang.callmonitor.Call
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CallRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("permissionsBroadcast") private val permissionsBroadcast: MutableSharedFlow<String>,
) : Call.Repository,
    CoroutineScope {

    private val contentResolver get() = context.contentResolver

    private val handler by lazy { Handler(context.mainLooper) }

    override val coroutineContext = SupervisorJob()

    override val status = MutableStateFlow<Call.Ongoing?>(null)

    override val log = MutableStateFlow(emptyList<Call.Previous>())

    private val contentObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            log.update { getLog() }
        }
    }

    init {
        launch {
            permissionsBroadcast.distinctUntilChanged().filter { it == READ_CALL_LOG }.collect {
                contentResolver.registerContentObserver(CONTENT_URI, true, contentObserver)
                contentObserver.onChange(true)
            }
        }
        if (context.checkSelfPermission(READ_CALL_LOG) == PERMISSION_GRANTED) {
            permissionsBroadcast.tryEmit(READ_CALL_LOG)
        }
    }

    private fun getLog(): List<Call.Previous> = buildList {
        val projection = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )
        val sortOrder = CallLog.Calls.DATE + " DESC"
        context.contentResolver.query(
            CONTENT_URI, projection,
            null, null,
            sortOrder
        )?.use { cursor ->
            val nameIndex: Int = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberIndex: Int = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex: Int = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex: Int = cursor.getColumnIndex(CallLog.Calls.DURATION)
            while (cursor.moveToNext()) {
                val name: String? = cursor.getString(nameIndex)
                val number: String = cursor.getString(numberIndex)
                val dateMillis: Long = cursor.getLong(dateIndex)
                val duration: Long = cursor.getLong(durationIndex)
                val entry = Call.Previous(
                    name = name?.takeIf { it.isNotBlank() },
                    number = number,
                    beginning = Date(dateMillis),
                    duration = duration,
                    timesQueried = 1,
                )
                add(entry)
            }
        }
    }
}
