package com.babyvault.android.domain.model

import java.time.Instant

enum class FeedType { BREAST, BOTTLE, SOLID }

data class FeedLog(
    val id: String,
    val feedType: FeedType,
    val amountMl: Int?,
    val durationMinutes: Int?,
    val side: String?,
    val notes: String,
    val loggedAt: Instant,
)
