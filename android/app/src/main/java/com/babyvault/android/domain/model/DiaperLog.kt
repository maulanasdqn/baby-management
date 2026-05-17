package com.babyvault.android.domain.model
import java.time.Instant
enum class DiaperType { WET, DIRTY, BOTH }
data class DiaperLog(
    val id: String,
    val diaperType: DiaperType,
    val notes: String,
    val loggedAt: Instant,
)
