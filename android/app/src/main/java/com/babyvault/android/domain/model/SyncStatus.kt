package com.babyvault.android.domain.model

import java.time.Instant

data class SyncStatus(
    val pendingMilestones: Int,
    val pendingGrowthLogs: Int,
    val pendingMediaItems: Int,
    val lastSyncedAt: Instant?,
    val isConfigured: Boolean,
) {
    val totalPending: Int get() = pendingMilestones + pendingGrowthLogs + pendingMediaItems
}
