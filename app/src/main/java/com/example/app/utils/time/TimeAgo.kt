package com.example.app.utils.time


fun formatTimeAgo(createdAt: Long): String {
    val now = System.currentTimeMillis()

    if (createdAt <= 0L || createdAt > now) return "Vừa xong"

    val diff = now - createdAt

    if (diff < 60_000) return "Vừa xong"

    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val years = days / 365

    return when {
        minutes < 60 -> "$minutes phút"
        hours < 24   -> "$hours giờ"
        days < 7     -> "$days ngày"
        weeks < 52   -> "$weeks tuần"
        else         -> "$years năm"
    }
}
