package com.babyvault.android.presentation.utils
fun formatSleepMinutes(minutes: Long): String {
    if (minutes == 0L) return "—"
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h${if (m > 0) " ${m}m" else ""}" else "${m}m"
}
fun formatElapsedSeconds(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
fun formatAvgSleep(avgMinutes: Long): String {
    if (avgMinutes == 0L) return "No data"
    return "${avgMinutes / 60}h ${avgMinutes % 60}m"
}
fun formatBarLabel(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h" else "${m}m"
}
