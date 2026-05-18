package com.babyvault.android.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_logs")
data class FeedLogEntity(
    @PrimaryKey val id: String,
    val feedType: String,
    val amountMl: Int?,
    val durationMinutes: Int?,
    val side: String?,
    val notes: String,
    val loggedAt: Long,
)

@Entity(tableName = "sleep_logs")
data class SleepLogEntity(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long,
    val notes: String,
    val durationMinutes: Long,
)

@Entity(tableName = "diaper_logs")
data class DiaperLogEntity(
    @PrimaryKey val id: String,
    val diaperType: String,
    val notes: String,
    val loggedAt: Long,
)

@Entity(tableName = "growth_logs")
data class GrowthLogEntity(
    @PrimaryKey val id: String,
    val weightGrams: Int?,
    val heightMm: Int?,
    val notes: String,
    val loggedAt: Long,
)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val occurredAt: Long,
    val createdAt: Long,
)

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val encryptedPath: String,
    val sizeBytes: Long,
    val createdAt: Long,
)
