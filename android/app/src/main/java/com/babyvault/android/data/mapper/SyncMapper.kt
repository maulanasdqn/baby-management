package com.babyvault.android.data.mapper
import com.babyvault.android.domain.model.SyncStatus
import com.babyvault.core.SyncStatusDto
import java.time.Instant
fun SyncStatusDto.toDomain(): SyncStatus = SyncStatus(
    pendingMilestones = pendingMilestones.toInt(),
    pendingGrowthLogs = pendingGrowthLogs.toInt(),
    pendingMediaItems = pendingMediaItems.toInt(),
    lastSyncedAt = lastSyncedAtMillis?.let { Instant.ofEpochMilli(it) },
    isConfigured = isConfigured,
)
