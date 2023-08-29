package cc.jang.callmonitor.android

import android.Manifest.permission.READ_CALL_LOG
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.room.CallRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepository @Inject constructor(
    private val serverState: Call.Server.State,
    private val permissionsStore: PermissionsStore,
    private val callLogResolver: CallLogResolver,
    private val callLogObserver: CallLogObserver,
    private val timestampDao: CallRoom.TimestampDao,
    private val contactNameResolver: ContactNameResolver,
) : Call.Repository,
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {

    val statusState = MutableStateFlow<Call.Status?>(null)

    override val status: Call.Status?
        get() = statusState.value?.apply {
            if (ongoing)
                insertTimestamp()
        }

    override val log = MutableStateFlow(emptyList<Call.Log>())

    init {
        launch {
            // Getting READ_CALL_LOG permission is sufficient to start getting call logs.
            permissionsStore.first { it[READ_CALL_LOG] == true }
            flowOf(
                permissionsStore,
                serverState,
                callLogObserver.flow(),
            ).flattenMerge().collect {
                log.update { getLog() }
            }
        }
    }

    /**
     * Insert timestamp related to the phone number into the data base.
     */
    private fun Call.Status.insertTimestamp() = launch {
        val timestamp = CallRoom.Timestamp(
            timestamp = System.currentTimeMillis(),
            number = number,
        )
        timestampDao.insert(timestamp)
    }

    /**
     * Fetch and return list of [Call.Log] only if the call server is currently running,
     * otherwise return empty list
     */
    private suspend fun getLog(): List<Call.Log> =
        when (val status = serverState.value) {
            !is Call.Server.Status.Started -> emptyList()
            else -> callLogResolver.resolve(status.date).map { log ->
                log.update()
            }
        }

    /**
     * Update [Call.Log] for additional info like:
     * - name related to the phone number.
     * - how many times the ongoing call was queried from API.
     */
    private suspend fun Call.Log.update(): Call.Log {
        val name = name?.takeIf { it.isNotBlank() }
            ?: contactNameResolver.resolve(number)
        val timesQueried = timestampDao.getCount(
            number = number,
            startTime = beginning.time,
            endTime = beginning.time + duration * 1000
        )
        return copy(
            name = name,
            timesQueried = timesQueried
        )
    }
}
