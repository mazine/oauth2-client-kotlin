package jetbrains.hub.oauth2.client

import java.text.SimpleDateFormat
import java.util.*

val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
fun String.toCalendar() = Calendar.getInstance().apply {
    time = simpleDateFormat.parse(this@toCalendar)
}

fun Calendar.asString() = simpleDateFormat.format(time)
