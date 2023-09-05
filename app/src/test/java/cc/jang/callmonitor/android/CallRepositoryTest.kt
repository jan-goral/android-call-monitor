package cc.jang.callmonitor.android

import android.Manifest.permission.READ_CALL_LOG
import cc.jang.callmonitor.Call
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class CallRepositoryTest {

    @RelaxedMockK
    lateinit var permissionsStore: PermissionsStore

    @RelaxedMockK
    lateinit var callLogResolver: CallLogResolver

    @RelaxedMockK
    lateinit var callLogObserver: CallLogObserver

    @RelaxedMockK
    lateinit var timesQueriedStore: TimesQueriedStore

    @RelaxedMockK
    lateinit var contactNameResolver: ContactNameResolver

    lateinit var callRepository: CallRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { permissionsStore.state } returns MutableStateFlow(mapOf(READ_CALL_LOG to true))
    }

    @Test
    fun updateAndGetStatus() = runTest {
        // given
        val given = Call.Status(ongoing = true)
        every { callLogResolver.resolve(any()) } returns emptyList()
        coEvery { timesQueriedStore.consumeTimesQueried(any()) } returns mockk()
        callRepository = CallRepository(
            permissionsStore = permissionsStore,
            callLogResolver = callLogResolver,
            callLogObserver = callLogObserver,
            timesQueriedStore = timesQueriedStore,
            contactNameResolver = contactNameResolver,
            dispatcher = StandardTestDispatcher(testScheduler),
        )
        advanceUntilIdle()

        // when
        callRepository.updateStatus { given }
        advanceUntilIdle()
        val actual = callRepository.status
        advanceUntilIdle()

        // then
        assertSame(given, actual)
        coVerify(exactly = 1) { timesQueriedStore.bumpQueries(given) }
    }

    @Test
    fun getLog() = runTest {
        // given
        val expected = listOf(Call.Log())
        val getTimesQueried = mockk<suspend (TimesQueriedStore.PhoneId) -> Int>()
        coEvery { getTimesQueried(any()) } returns 0
        coEvery { timesQueriedStore.consumeTimesQueried(any()) } returns getTimesQueried
        every { callLogResolver.resolve(any()) } returns listOf(CallLogResolver.Log())
        every { contactNameResolver.resolve(any()) } returns null
        callRepository = CallRepository(
            permissionsStore = permissionsStore,
            callLogResolver = callLogResolver,
            callLogObserver = callLogObserver,
            timesQueriedStore = timesQueriedStore,
            contactNameResolver = contactNameResolver,
            dispatcher = StandardTestDispatcher(testScheduler),
        )
        advanceUntilIdle()

        // when
        val actual = callRepository.log.value

        // then
        assertEquals(expected, actual)
        coVerify(exactly = 1) { getTimesQueried(any()) }
        coVerify(exactly = 1) { timesQueriedStore.consumeTimesQueried(any()) }
        verify(exactly = 1) { callLogResolver.resolve(any()) }
        verify(exactly = 1) { contactNameResolver.resolve(any()) }
    }
}
