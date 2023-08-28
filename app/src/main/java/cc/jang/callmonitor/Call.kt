package cc.jang.callmonitor

import kotlinx.coroutines.flow.StateFlow
import java.net.URI
import java.text.DateFormat
import java.util.Date

object Call {
    data class Ongoing(
        val outgoing: Boolean = true,
        val ongoing: Boolean = true,
        val number: String = "",
        val name: String = "",
    )

    data class Previous(
        val beginning: Date = Date(0),
        val duration: Long = 0,
        val number: String = "",
        val name: String? = null,
        val timesQueried: Int = 0,
    )

    interface Repository {
        val status: Ongoing?
        val log: StateFlow<List<Previous>>
    }

    interface Api : Repository {

        val config: Config
        val address: StateFlow<URI>

        data class Config(
            val port: Int,
            val dateFormat: DateFormat,
        )

        fun getMetadata(): Metadata

        data class Metadata(
            val start: Date? = null,
            val services: List<Service> = emptyList(),
        )

        data class Service(
            val name: String = "",
            val uri: URI = URI(""),
        )
    }

    object Server {

        interface State : StateFlow<Status>

        sealed class Status {
            val date = Date()

            class Syncing : Status()
            class Started : Status()
            class Stopped : Status()
        }
    }
}
