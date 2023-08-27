package cc.jang.callmonitor.android

import android.Manifest.permission.READ_CONTACTS
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI
import android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME


fun Context.getContactName(phoneNumber: String?): String? {
    checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED || return null
    phoneNumber ?: return null
    val uri = Uri.withAppendedPath(
        CONTENT_FILTER_URI,
        Uri.encode(phoneNumber)
    )
    var contactName: String? = null
    val cursor: Cursor? = contentResolver.query(uri, contactNameProjection, null, null, null)
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(0)
        }
        cursor.close()
    }
    return contactName
}

private val contactNameProjection = arrayOf(DISPLAY_NAME)
