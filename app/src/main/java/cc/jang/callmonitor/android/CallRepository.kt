package cc.jang.callmonitor.android

import android.Manifest.permission.READ_CALL_LOG
import android.util.Log
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.android.TimesQueriedStore.PhoneId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CallRepository @Inject constructor(
    private val permissionsStore: PermissionsStore,
    private val callLogResolver: CallLogResolver,
    private val callLogObserver: CallLogObserver,
    private val timesQueriedStore: TimesQueriedStore,
    private val contactNameResolver: ContactNameResolver,
    @Named("call_repo_dispatcher") dispatcher: CoroutineDispatcher,
) : Call.Repository,
    CoroutineScope by CoroutineScope(SupervisorJob() + dispatcher) {

    private companion object {
        val Tag: String = CallRepository::class.java.simpleName
    }

    private val statusState = MutableStateFlow<Call.Status?>(null)

    override val status: Call.Status?
        get() = statusState.value?.apply {
            if (ongoing) {
                Log.d(Tag, "${copy(time = Date())} - get")
                launch {
                    timesQueriedStore.bumpQueries(this@apply)
                }
            }
        }

    fun updateStatus(block: (Call.Status?) -> Call.Status?) {
        launch {
            statusState.update(block)
        }
    }

    override val log = MutableStateFlow(emptyList<Call.Log>())

    init {
        launch {
            timesQueriedStore.init()
            // Getting READ_CALL_LOG permission is sufficient to start getting call logs.
            permissionsStore.state.first { it[READ_CALL_LOG] == true }
            flowOf(
                permissionsStore.state,
                callLogObserver.flow(),
            ).flattenMerge().collect {
                log.update { getLog() }
            }
        }
    }

    /**
     * Fetch and return list of [Call.Log] only if the call server is currently running,
     * otherwise return empty list.
     */
    private suspend fun getLog(): List<Call.Log> {
        val logs = callLogResolver.resolve(Date(0))
        val ids = logs.map(CallLogResolver.Log::id)
        val getTimesQueried = timesQueriedStore.consumeTimesQueried(ids)
        return logs.map { log ->
            Call.Log(
                id = log.id,
                beginning = log.beginning,
                duration = log.duration,
                number = log.number,
                name = log.name ?: contactNameResolver.resolve(log.number),
                timesQueried = getTimesQueried(PhoneId(log.number, log.id)),
            )
        }.onEach {
            Log.d(Tag, "$it")
        }
    }
}
