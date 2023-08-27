package cc.jang.callmonitor.date

import org.junit.Test
import java.text.ParseException
import java.util.Date
import kotlin.test.assertEquals

class CallDateFormatCompatApi23Test {

    private val dateFormat = CallDateFormatCompatApi23()

    @Test
    fun test_format() {
        val str = dateFormat.format(Date(0))
        assertEquals(':', str[str.lastIndex-2])
    }

    @Test
    fun test_parse_success() {
        dateFormat.parse(ZoneXXX)
    }

    @Test(expected = ParseException::class)
    fun test_parse_failure() {
        dateFormat.parse(ZoneZ)
    }

    @Test
    fun test_convertXXXtoZ() {
        val expected = ZoneZ
        val actual = CallDateFormatCompatApi23.convertXXXtoZ(ZoneXXX)

        assertEquals(expected, actual)
    }

    @Test
    fun test_convertZtoXXX() {
        val expected = ZoneXXX
        val actual = CallDateFormatCompatApi23.convertZtoXXX(ZoneZ)

        assertEquals(expected, actual)
    }

    companion object {
        const val ZoneXXX = "2018-05-02T23:00:00+00:00"
        const val ZoneZ = "2018-05-02T23:00:00+0000"
    }
}
