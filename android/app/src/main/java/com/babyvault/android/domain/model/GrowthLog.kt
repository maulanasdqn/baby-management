package com.babyvault.android.domain.model

import java.time.Instant

data class GrowthLog(
    val id: String,
    val weightGrams: Int?,
    val heightMm: Int?,
    val notes: String,
    val loggedAt: Instant,
)
