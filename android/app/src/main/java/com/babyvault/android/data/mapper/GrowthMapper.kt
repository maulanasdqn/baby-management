package com.babyvault.android.data.mapper
import com.babyvault.android.domain.model.GrowthLog
import com.babyvault.core.GrowthLogDto
import java.time.Instant
fun GrowthLogDto.toDomain() = GrowthLog(
    id = id,
    weightGrams = weightGrams?.toInt(),
    heightMm = heightMm?.toInt(),
    notes = notes,
    loggedAt = Instant.ofEpochMilli(loggedAtMillis),
)
