package com.galaxykud.notes.presentation.utils

import android.icu.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

object DateFormatter {

    private val millisInHour = TimeUnit.HOURS.toMillis(1)
    private val millisInDay = TimeUnit.DAYS.toMillis(1)
    private val formatter = SimpleDateFormat.getDateInstance(DateFormat.SHORT)

    fun formatDateToString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < millisInHour -> "Just now"
            diff < millisInDay -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours h ago"
            }
            else -> {
                formatter.format(timestamp)
            }
        }
    }
}