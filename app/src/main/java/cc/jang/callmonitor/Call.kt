package cc.jang.callmonitor

import kotlinx.coroutines.flow.MutableStateFlow
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
        val status: StateFlow<Ongoing?>
        val log: StateFlow<List<Previous>>
    }

    interface Api : Repository {

        val config: Config
        val state: StateFlow<Status>
        val address: StateFlow<URI>

        data class Config(
            val port: Int,
            val dateFormat: DateFormat,
        )

        fun getMetadata(): Metadata

        data class Metadata(
            val start: Date = Date(0),
            val services: List<Service> = emptyList(),
        )

        data class Service(
            val name: String = "",
            val uri: URI = URI(""),
        )

        interface State: MutableStateFlow<Status>

        enum class Status { Syncing, Started, Stopped }
    }
}
