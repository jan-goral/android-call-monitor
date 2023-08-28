package cc.jang.callmonitor.android

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_CALL_LOG
import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.READ_PHONE_STATE
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionsStore(
    private val context: Context,
    private val state: MutableStateFlow<Map<String, Boolean>>,
) : StateFlow<Map<String, Boolean>> by state {

    @Inject
    constructor(@ApplicationContext context: Context) :
        this(context, MutableStateFlow(emptyMap()))

    private val required = listOf(
        READ_CALL_LOG,
        READ_PHONE_STATE,
        READ_CONTACTS,
    ) + buildList {
        if (SDK_INT >= TIRAMISU) add(POST_NOTIFICATIONS)
    }

    val granted get() = value.filterValues { it }.keys
    val missing get() = value.filterValues { !it }.keys

    init {
        update()
    }

    fun update() {
        state.update {
            required.associateWith {
                context.checkSelfPermission(it) == PERMISSION_GRANTED
            }
        }
    }
}
