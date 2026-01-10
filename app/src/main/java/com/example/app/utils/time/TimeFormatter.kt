package com.example.app.utils.time


import android.util.Log
import java.time.Instant

fun formatTimeAgoFromIso(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "Vừa xong"
    return try {
        val instant = Instant.parse(isoString)
        formatTimeAgo(instant.toEpochMilli())
    } catch (e: Exception) {
        "Vừa xong"
    }
}