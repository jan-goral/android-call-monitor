package cc.jang.callmonitor.android

import cc.jang.callmonitor.Call
import cc.jang.callmonitor.room.CallRoom
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TimesQueriedStoreTest {

    lateinit var dao: CallRoom.TimesQueriedDao
    lateinit var store: TimesQueriedStore

    @BeforeTest
    fun setUp() {
        dao = mockk(relaxed = true)
        store = TimesQueriedStore(dao)
    }

    @Test
    fun bumpAndConsumeTimesQueried() = runBlocking {
        // given
        val status1 = Call.Status(number = "1")
        val status2 = Call.Status(number = "2")
        val ids = listOf(1L, 2, 3)
        val phoneIds = listOf(
            TimesQueriedStore.PhoneId(id = 1, number = "1"),
            TimesQueriedStore.PhoneId(id = 2, number = "2"),
            TimesQueriedStore.PhoneId(id = 3, number = "3"),
        )

        // when
        store.bumpQueries(status1)
        store.bumpQueries(status2)
        store.bumpQueries(status1)

        val get = store.consumeTimesQueried(ids)

        // Then
        assertEquals(2, get(phoneIds[0]))
        assertEquals(1, get(phoneIds[1]))
        assertEquals(0, get(phoneIds[2]))
        coVerify(exactly = 1) { dao.getByIds(any()) }
        coVerify(exactly = 5) { dao.insert(any()) }
    }

    @Test
    fun bumpTimesQueriedConcurrent() = runBlocking {
        val times = 10000
        val status = Call.Status(number = "1")
        val phoneId = TimesQueriedStore.PhoneId(id = 1, number = "1")

        val dispatcher = Dispatchers.IO
        (1..times).map {
            launch(dispatcher) {
                store.bumpQueries(status)
            }
        }.joinAll()

        val getTimes = store.consumeTimesQueried(listOf(1))

        coVerify(exactly = times) { dao.insert(any()) }
        val actual = getTimes(phoneId)
        assertEquals(times, actual)
    }
}
