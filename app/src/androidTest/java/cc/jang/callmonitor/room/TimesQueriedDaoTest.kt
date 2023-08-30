package cc.jang.callmonitor.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class TimesQueriedDaoTest {

    private lateinit var database: CallRoom.DB
    private lateinit var dao: CallRoom.TimesQueriedDao

    @BeforeTest
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CallRoom.DB::class.java
        ).build()
        dao = database.timesQueriedDao
    }

    @AfterTest
    fun closeDatabase() {
        database.clearAllTables()
        database.close()
    }

    @Test
    fun insertAndGetByIds() = runBlocking {
        // given
        val ids = (1..10L).toList()
        val expected = ids.map { id ->
            CallRoom.TimesQueried(
                id = id,
                number = "$id",
                count = id.toInt(),
            ).also { timesQueried ->
                dao.insert(timesQueried)
            }
        }

        // when
        val actual = dao.getByIds(ids)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun insertAndGetByPhoneId() = runBlocking {
        // given
        val expected = CallRoom.TimesQueried(
            id = 0,
            number = "123",
            count = 0,
        )
        dao.insert(expected)

        // when
        val actual = dao.getByPhoneId("123")

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun getByPhoneId_null() = runBlocking {
        // when
        val actual = dao.getByPhoneId("123")

        // then
        assertEquals(null, actual)
    }
}
