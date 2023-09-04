package cc.jang.callmonitor.android

import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler
import androidx.core.content.getSystemService
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.Ip
import cc.jang.callmonitor.date.CallDateFormatCompatApi23
import cc.jang.callmonitor.domain.CallService
import cc.jang.callmonitor.room.CallRoom
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors.*
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun callServiceConfig() = Call.Service.Config(
        port = 12345,
        dateFormat = CallDateFormatCompatApi23(),
    )

    @Provides
    fun timesQueriedDao(db: CallRoom.DB) = db.timesQueriedDao

    @Provides
    fun contentResolver(@ApplicationContext context: Context) = context.contentResolver!!

    @Provides
    fun handler(@ApplicationContext context: Context) = Handler(context.mainLooper)

    @Provides
    fun connectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getSystemService()!!

    @Provides
    @Named("call_repo_dispatcher")
    @Singleton
    fun repoDispatcher(): CoroutineDispatcher = newSingleThreadExecutor().asCoroutineDispatcher()


    @Module
    @InstallIn(SingletonComponent::class)
    interface Bindings {

        @Binds
        fun callApi(api: CallService): Call.Service

        @Binds
        fun callRepo(repository: CallRepository): Call.Repository

        @Binds
        fun ipRepo(repository: IpRepository): Ip.Repository

        @Binds
        fun callServerState(state: ForegroundService.State): Call.Server.State
    }
}
