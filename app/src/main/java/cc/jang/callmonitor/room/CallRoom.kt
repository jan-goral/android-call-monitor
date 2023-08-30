package cc.jang.callmonitor.room

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object CallRoom {

    @Singleton
    @Provides
    fun createDatabase(
        @ApplicationContext context: Context,
    ) = Room.databaseBuilder(
        context.applicationContext,
        DB::class.java,
        "call_database"
    ).fallbackToDestructiveMigration().build()

    @Database(
        version = 1,
        entities = [TimesQueried::class],
    )
    abstract class DB : RoomDatabase() {
        abstract val timesQueriedDao: TimesQueriedDao
    }

    @Dao
    interface TimesQueriedDao {

        @Upsert
        suspend fun insert(timesQueried: TimesQueried)

        @Query("SELECT * FROM times_queried WHERE id IN (:ids)")
        suspend fun getByIds(ids: List<Long>): List<TimesQueried>

        @Query("SELECT * FROM times_queried WHERE id = :id AND number = :number")
        suspend fun getByPhoneId(number: String, id: Long = 0): TimesQueried?
    }

    @Entity(tableName = "times_queried", primaryKeys = ["id", "number"])
    data class TimesQueried(
        val number: String,
        val id: Long = 0,
        val count: Int = 0,
        val start: Long = 0
    )
}
