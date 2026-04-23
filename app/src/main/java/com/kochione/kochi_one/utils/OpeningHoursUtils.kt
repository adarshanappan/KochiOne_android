package com.kochione.kochi_one.utils

import com.kochione.kochi_one.models.DayHours

/**
 * Parses "HH:mm" or "HH:mm:ss" to minutes from midnight.
 */
fun parseTimeToMinutes(hhmm: String?): Int? {
    if (hhmm.isNullOrBlank()) return null
    val parts = hhmm.trim().split(":")
    if (parts.size < 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val minutePart = parts[1].filter { it.isDigit() }.take(2)
    val m = minutePart.toIntOrNull() ?: parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}

/**
 * Whether the venue is open right now for this [DayHours], using device local time.
 * - Respects [DayHours.closed].
 * - Same-day window: open < close → open when now in [open, close).
 * - Overnight window: open > close → open when now >= open OR now < close.
 * - If times cannot be parsed, returns false (showing as closed).
 */
fun DayHours.isOpenAtNow(nowMinutes: Int): Boolean {
    if (closed) return false
    val openM = parseTimeToMinutes(open)
    val closeM = parseTimeToMinutes(close)
    if (openM == null || closeM == null) return false
    if (openM == closeM) return false
    return if (openM < closeM) {
        nowMinutes in openM until closeM
    } else {
        nowMinutes >= openM || nowMinutes < closeM
    }
}

/**
 * Minutes until closing if currently open; null if unknown or not applicable.
 */
fun DayHours.minutesUntilCloseIfOpen(nowMinutes: Int): Int? {
    if (closed) return null
    val openM = parseTimeToMinutes(open)
    val closeM = parseTimeToMinutes(close)
    if (openM == null || closeM == null) return null
    if (openM == closeM) return null
    if (!isOpenAtNow(nowMinutes)) return null

    return if (openM < closeM) {
        closeM - nowMinutes
    } else {
        // overnight
        if (nowMinutes >= openM) {
            (24 * 60 - nowMinutes) + closeM
        } else {
            closeM - nowMinutes
        }
    }
}
