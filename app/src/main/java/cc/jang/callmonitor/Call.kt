package cc.jang.callmonitor

import kotlinx.coroutines.flow.StateFlow
import java.net.URI
import java.text.DateFormat
import java.util.Date

/**
 * The core abstraction of the phone calls.
 */
object Call {

    /**
     * Status of the current phone call (ongoing or ringing).
     */
    data class Status(
        val time: Date = Date(0),
        val outgoing: Boolean = true,
        val ongoing: Boolean = true,
        val number: String = "",
        val name: String? = null,
    )

    /**
     * Information about the past phone call.
     */
    data class Log(
        val id: Long = 0,
        val beginning: Date = Date(0),
        val duration: Long = 0,
        val number: String = "",
        val name: String? = null,
        val timesQueried: Int = 0,
    )

    interface Repository {
        val status: Status?
        val log: StateFlow<List<Log>>
    }

    /**
     * Provides all data related to the phone calls necessary for exposing to the users.
     */
    interface Service : Repository {

        val config: Config
        val address: StateFlow<URI>
        val metadata: Metadata

        data class Config(
            val port: Int,
            val dateFormat: DateFormat,
        )

        data class Metadata(
            val start: Date? = null,
            val services: List<Endpoint> = emptyList(),
        )

        data class Endpoint(
            val name: String = "",
            val uri: URI = URI(""),
        )
    }

    /**
     * The core abstraction of the call server.
     */
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
