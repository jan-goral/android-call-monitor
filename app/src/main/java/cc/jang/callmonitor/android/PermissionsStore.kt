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

/**
 * Responsible for storing and providing information about the status of permissions required by the application.
 */
@Singleton
class PermissionsStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val _state = MutableStateFlow(emptyMap<String, Boolean>())

    private val required = listOf(
        READ_CALL_LOG,
        READ_PHONE_STATE,
        READ_CONTACTS,
    ) + buildList {
        if (SDK_INT >= TIRAMISU) add(POST_NOTIFICATIONS)
    }

    val granted get() = state.value.filterValues { it }.keys
    val missing get() = state.value.filterValues { !it }.keys

    init {
        refresh()
    }

    val state: StateFlow<Map<String, Boolean>> get() = _state

    /**
     * Gathers fresh information about permissions status.
     */
    fun refresh() {
        _state.update {
            required.associateWith {
                context.checkSelfPermission(it) == PERMISSION_GRANTED
            }
        }
    }
}
