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
class TimestampDaoTest {

    private lateinit var database: CallRoom.DB
    private lateinit var imageDao: CallRoom.TimestampDao

    @BeforeTest
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CallRoom.DB::class.java
        ).build()
        imageDao = database.timestampDao
    }

    @AfterTest
    fun closeDatabase() {
        database.clearAllTables()
        database.close()
    }

    @Test
    fun insertAndCountTimestamps() = runBlocking {
        val number = "123"
        (1..10L).forEach { t ->
            val timestamp = CallRoom.Timestamp(number = number, timestamp = t)
            imageDao.insert(timestamp)
        }

        val actual = imageDao.getCount(number, 1L, 10L)

        assertEquals(10, actual)
    }
}
