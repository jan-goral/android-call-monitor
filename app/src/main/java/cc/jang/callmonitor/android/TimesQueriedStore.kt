package cc.jang.callmonitor.android

import cc.jang.callmonitor.Call
import cc.jang.callmonitor.room.CallRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimesQueriedStore @Inject constructor(
    private val dao: CallRoom.TimesQueriedDao,
) {
    private val current = MutableStateFlow(emptyMap<String, CallRoom.TimesQueried>())

    /**
     * Loads cached unassigned list of [CallRoom.TimesQueried] values into memory.
     */
    suspend fun init() {
        current.update {
            dao.getByIds(listOf(0))
                .associateBy { it.number }
        }
    }

    /**
     * Increments queries count for given [Call.Status].
     */
    suspend fun bumpQueries(status: Call.Status) {
        val number = status.number
        current.getAndUpdate { map ->

            // get value or create new
            var value = map[number] ?: CallRoom.TimesQueried(
                number = number,
                start = System.currentTimeMillis(),
            )

            // increment queries count
            value = value.copy(
                count = value.count + 1
            )

            // save to db
            dao.insert(value)

            // update memory cache
            map + (number to value)
        }
    }

    /**
     * Consumes unassigned TimesQueried and returns provider for both unassigned and assigned times queried value.
     */
    suspend fun consumeTimesQueried(ids: List<Long>): suspend (PhoneId) -> Int {

        // consume the map of newest TimesQueried not assigned to any Call.Log id
        val unassigned: MutableMap<String, CallRoom.TimesQueried> = current
            .getAndUpdate { emptyMap() }
            .toMutableMap()

        // resolve map of previous TimesQueried already assigned to Call.Log id
        val assigned: Map<Long, CallRoom.TimesQueried> = dao.getByIds(ids)
            .associateBy { queried -> queried.id }

        return { log ->

            // consume unassigned TimesQueried related to the log number if exist
            val times = unassigned.remove(log.number)

                // assign the log id to TimesQueried entity and save into database
                ?.apply { dao.insert(copy(id = log.id)) }

            // otherwise get assigned by log id
                ?: assigned[log.id]

            // update log timesQueried if needed
            times?.count ?: 0
        }
    }

    data class PhoneId(val number: String, val id: Long)
}
