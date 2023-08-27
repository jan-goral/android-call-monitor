package cc.jang.callmonitor.date

import java.text.FieldPosition
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Android API 23 doesn't support `XXX` format for zone.
 * This implementation provides required compatibility with format `yyyy-MM-dd'T'HH:mm:ssXXX`.
 * Converts `Z` to `XXX` by adding `:` character in string representation of zone.
 */
class CallDateFormatCompatApi23 : SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US) {

    override fun format(
        date: Date,
        toAppendTo: StringBuffer,
        fieldPosition: FieldPosition,
    ): StringBuffer {
        val value = super.format(date, toAppendTo, fieldPosition)
        val fixed = convertZtoXXX(value.toString())
        return StringBuffer().append(fixed)
    }

    override fun parse(source: String, pos: ParsePosition): Date? {
        val fixed = convertXXXtoZ(source)
        return super.parse(fixed, pos)
    }

    companion object {
        fun convertXXXtoZ(date: String): String =
            date.toMutableList().apply { removeAt(lastIndex - 2) }.joinToString("")

        fun convertZtoXXX(date: String): String =
            date.toMutableList().apply { add(lastIndex - 1, ':') }.joinToString("")
    }
}
