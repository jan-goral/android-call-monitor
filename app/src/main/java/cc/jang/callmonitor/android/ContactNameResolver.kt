package cc.jang.callmonitor.android

import android.Manifest.permission.READ_CONTACTS
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI
import android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ContactNameResolver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentResolver: ContentResolver,
) {
    fun resolve(phoneNumber: String?): String? {
        phoneNumber ?: return null
        context.checkSelfPermission(READ_CONTACTS) == PERMISSION_GRANTED || return null
        val uri = Uri.withAppendedPath(CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        var name: String? = null
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                name = cursor.getString(0)
            }
        }
        return name
    }

    private companion object {
        private val projection = arrayOf(DISPLAY_NAME)
    }
}
