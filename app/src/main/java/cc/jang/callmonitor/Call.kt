package cc.jang.callmonitor

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
        val name: String = "",
        val timesQueried: Int = 0,
    )

    interface Repository {
        fun getStatus(): Ongoing
        fun getLog(): List<Previous>
    }

    interface Api : Repository {

        val config: Config

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
    }
}