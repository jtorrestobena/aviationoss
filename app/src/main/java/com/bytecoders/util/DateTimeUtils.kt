package com.bytecoders.util

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    fun formatTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "--:--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            val date = parser.parse(isoString) ?: return "--:--"
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = parser.parse(isoString) ?: return "--:--"
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                formatter.format(date)
            } catch (e2: Exception) {
                isoString.substringAfter("T").take(5)
            }
        }
    }

    fun formatTimeDetailed(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            val date = parser.parse(isoString) ?: return "--"
            val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = parser.parse(isoString) ?: return "--"
                val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                formatter.format(date)
            } catch (e2: Exception) {
                isoString.replace("T", " ").take(16)
            }
        }
    }
}
