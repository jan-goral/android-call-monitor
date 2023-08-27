package cc.jang.callmonitor.android

import android.Manifest.permission.READ_CALL_LOG
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.provider.CallLog
import android.provider.CallLog.Calls.CONTENT_URI
import cc.jang.callmonitor.Call
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionsRepo: PermissionsRepository,
    private val timestampDB: TimestampRoom.DB,
) : Call.Repository,
    CoroutineScope {

    private val contentResolver get() = context.contentResolver

    private val handler by lazy { Handler(context.mainLooper) }

    val statusState = MutableStateFlow<Call.Ongoing?>(null)

    override val coroutineContext = SupervisorJob() + Dispatchers.IO

    override val status: Call.Ongoing?
        get() = statusState.value?.apply {
            if (ongoing) launch {
                val timestamp = TimestampRoom.Timestamp(
                    timestamp = System.currentTimeMillis(),
                    number = number,
                )
                timestampDB.timestampDao.insert(timestamp)
            }
        }

    override val log = MutableStateFlow(emptyList<Call.Previous>())

    private val contentObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            launch { log.update { getLog() } }
        }
    }

    init {
        launch {
            permissionsRepo.first { it[READ_CALL_LOG] == true }
            contentResolver.registerContentObserver(CONTENT_URI, true, contentObserver)
            permissionsRepo.collect {
                contentObserver.onChange(true)
            }
        }
    }

    private suspend fun getLog(): List<Call.Previous> = buildList {
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
            val dao = timestampDB.timestampDao
            val nameIndex: Int = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberIndex: Int = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex: Int = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex: Int = cursor.getColumnIndex(CallLog.Calls.DURATION)
            while (cursor.moveToNext()) {
                val cachedName: String? = cursor.getString(nameIndex)
                val number: String = cursor.getString(numberIndex)
                val dateMillis: Long = cursor.getLong(dateIndex)
                val duration: Long = cursor.getLong(durationIndex)
                val name = cachedName?.takeIf { it.isNotBlank() } ?: context.getContactName(number)
                val timesQueried = dao.getCount(number, dateMillis, dateMillis + duration * 1000)
                val entry = Call.Previous(
                    name = name,
                    number = number,
                    beginning = Date(dateMillis),
                    duration = duration,
                    timesQueried = timesQueried,
                )
                add(entry)
            }
        }
    }
}
