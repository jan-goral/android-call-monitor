package cc.jang.callmonitor.android

import android.content.ContentResolver
import android.provider.CallLog
import cc.jang.callmonitor.Call
import java.util.Date
import javax.inject.Inject

/**
 * Responsible for returning the list of the [Call.Log] since the given [Date].
 */
class CallLogResolver @Inject constructor(
    private val contentResolver: ContentResolver
) {
    private companion object {
        const val selection = "${CallLog.Calls.DATE} > ?"
        const val sortOrder = CallLog.Calls.DATE + " DESC"
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )
    }
    fun resolve(since: Date): List<Log> = buildList {
        val selectionArgs = arrayOf("${since.time}")
        contentResolver.query(
            CallLog.Calls.CONTENT_URI, projection,
            selection, selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idIndex: Int = cursor.getColumnIndex(CallLog.Calls._ID)
            val nameIndex: Int = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberIndex: Int = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex: Int = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex: Int = cursor.getColumnIndex(CallLog.Calls.DURATION)
            while (cursor.moveToNext()) {
                val id: Long = cursor.getLong(idIndex)
                val cachedName: String? = cursor.getString(nameIndex)
                val number: String = cursor.getString(numberIndex)
                // dateMillis is the date of RINGING state for incoming connections
                val dateMillis: Long = cursor.getLong(dateIndex)
                val duration: Long = cursor.getLong(durationIndex)
                val entry = Log(
                    id = id,
                    name = cachedName,
                    number = number,
                    beginning = Date(dateMillis),
                    duration = duration,
                )
                add(entry)
            }
        }
    }

    data class Log(
        val id: Long = 0,
        val name: String? = null,
        val beginning: Date = Date(0),
        val duration: Long = 0,
        val number: String = "",
    )
}
