package cc.jang.callmonitor.ui.screen

import android.telephony.PhoneNumberUtils
import java.util.Locale

fun String.parsePhoneNumber(): String {
    val countries = arrayOf(Locale.getDefault().isO3Country).plus(Locale.getISOCountries())
    val formatted = countries.firstNotNullOfOrNull { country ->
        PhoneNumberUtils.formatNumber(this, country)
            .takeIf { it != this }
    }
    return formatted ?: this
}
