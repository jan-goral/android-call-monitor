package cc.jang.callmonitor.room

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TimestampRoom {

    @Singleton
    @Provides
    fun createDatabase(
        @ApplicationContext context: Context,
    ) = Room.databaseBuilder(
        context.applicationContext,
        DB::class.java,
        "timestamp_database"
    ).fallbackToDestructiveMigration().build()

    @Database(
        version = 1,
        entities = [Timestamp::class],
    )
    abstract class DB : RoomDatabase() {
        abstract val timestampDao: TimestampDao
    }

    @Dao
    interface TimestampDao {
        @Insert
        suspend fun insert(timestamp: Timestamp)

        @Query(
            """
            SELECT COUNT(*) FROM timestamps 
            WHERE number = :number
            AND timestamp BETWEEN :startTime AND :endTime
            """
        )
        suspend fun getCount(number: String, startTime: Long, endTime: Long): Int
    }

    @Entity(tableName = "timestamps")
    data class Timestamp(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        val number: String,
        val timestamp: Long,
    )
}
