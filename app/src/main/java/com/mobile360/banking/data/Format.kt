package com.mobile360.banking.data

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** All money is USD, stored as cents. */
object Money {
    private val fmt = NumberFormat.getCurrencyInstance(Locale.US)
    fun format(cents: Long): String = fmt.format(cents / 100.0)
    fun formatSigned(cents: Long): String = (if (cents >= 0) "+" else "-") + fmt.format(kotlin.math.abs(cents) / 100.0)
    fun formatWhole(cents: Long): String = "$" + NumberFormat.getIntegerInstance(Locale.US).format(cents / 100)
}

object Times {
    /** Epoch millis for today + offsetDays at the given hour, never in the future. */
    fun dayAt(offsetDays: Long, hour: Int = 9): Long {
        val t = LocalDate.now().plusDays(offsetDays).atTime(hour, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return minOf(t, System.currentTimeMillis())
    }

    fun futureDayAt(offsetDays: Long, hour: Int = 9): Long =
        LocalDate.now().plusDays(offsetDays).atTime(hour, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun dateTime(millis: Long): String =
        DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.US).format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

    fun date(millis: Long): String =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US).format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

    fun shortDate(millis: Long): String =
        DateTimeFormatter.ofPattern("MMM d", Locale.US).format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

    /** "Today", "Yesterday" or "Oct 24" style label. */
    fun dayLabel(millis: Long): String {
        val d = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()
        return when (d) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> shortDate(millis)
        }
    }

    fun ago(millis: Long): String {
        val mins = ((System.currentTimeMillis() - millis) / 60_000).coerceAtLeast(0)
        return when {
            mins < 1 -> "just now"
            mins < 60 -> if (mins == 1L) "1 min ago" else "$mins mins ago"
            mins < 1440 -> "${mins / 60}h ago"
            else -> "${mins / 1440}d ago"
        }
    }

    fun plural(n: Int, singular: String, plural: String = singular + "s"): String =
        if (n == 1) "1 $singular" else "$n $plural"
}
