package cc.jang.callmonitor.android

import cc.jang.callmonitor.Call
import cc.jang.callmonitor.Ip
import cc.jang.callmonitor.domain.CallHandler
import cc.jang.callmonitor.mock.IpRepositoryMock
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import java.text.DateFormat
import java.util.Date
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Provides
    fun config() = Call.Api.Config(
        port = 12345,
        dateFormat = DateFormat.getDateTimeInstance(),
    )

    @Provides
    fun date() = Date()

    @Provides
    fun callApiState(callHandler: CallHandler): Call.Api.State = callHandler.state

    @Provides
    @Named("permissionsBroadcast")
    @Singleton
    fun permissionsBroadcast() = MutableSharedFlow<String>(replay = 10)

}

@Module
@InstallIn(SingletonComponent::class)
interface MainBindings {

    @Binds
    fun callApi(api: CallHandler): Call.Api

    @Binds
    fun callRepo(repository: CallRepository): Call.Repository

    @Binds
    fun ipRepo(repository: IpRepositoryMock): Ip.Repository
}

