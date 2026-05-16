package com.babyvault.android.domain.model

import java.time.Instant

data class SleepLog(
    val id: String,
    val startTime: Instant,
    val endTime: Instant,
    val notes: String,
    val durationMinutes: Long,
)
