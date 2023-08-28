package cc.jang.callmonitor.android

import android.content.ContentResolver
import android.provider.CallLog
import cc.jang.callmonitor.Call
import java.util.Date
import javax.inject.Inject

class CallLogResolver @Inject constructor(
    private val contentResolver: ContentResolver
) {
    private companion object {
        const val selection = "${CallLog.Calls.DATE} > ?"
        const val sortOrder = CallLog.Calls.DATE + " DESC"
        val projection = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )
    }
    fun resolve(since: Date) = buildList {
        val selectionArgs = arrayOf("${since.time}")
        contentResolver.query(
            CallLog.Calls.CONTENT_URI, projection,
            selection, selectionArgs,
            sortOrder
        )?.use { cursor ->
            val nameIndex: Int = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberIndex: Int = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex: Int = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex: Int = cursor.getColumnIndex(CallLog.Calls.DURATION)
            while (cursor.moveToNext()) {
                val cachedName: String? = cursor.getString(nameIndex)
                val number: String = cursor.getString(numberIndex)
                val dateMillis: Long = cursor.getLong(dateIndex)
                val duration: Long = cursor.getLong(durationIndex)
                val entry = Call.Log(
                    name = cachedName,
                    number = number,
                    beginning = Date(dateMillis),
                    duration = duration,
                    timesQueried = -1,
                )
                add(entry)
            }
        }
    }
}
