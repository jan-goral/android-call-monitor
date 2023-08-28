package cc.jang.callmonitor.android

import cc.jang.callmonitor.Call
import cc.jang.callmonitor.Ip
import cc.jang.callmonitor.date.CallDateFormatCompatApi23
import cc.jang.callmonitor.domain.CallHandler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Date

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Provides
    fun config() = Call.Api.Config(
        port = 12345,
        dateFormat = CallDateFormatCompatApi23(),
    )

    @Provides
    fun date() = Date()

}

@Module
@InstallIn(SingletonComponent::class)
interface MainBindings {

    @Binds
    fun callApi(api: CallHandler): Call.Api

    @Binds
    fun callRepo(repository: CallRepository): Call.Repository

    @Binds
    fun ipRepo(repository: IpRepository): Ip.Repository

    @Binds
    fun callServerState(state: CallService.State): Call.Server.State
}

