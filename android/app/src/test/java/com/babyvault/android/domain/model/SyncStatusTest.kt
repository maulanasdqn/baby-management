package com.babyvault.android.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class SyncStatusTest {

    private fun status(milestones: Int = 0, growthLogs: Int = 0, media: Int = 0) = SyncStatus(
        pendingMilestones = milestones,
        pendingGrowthLogs = growthLogs,
        pendingMediaItems = media,
        lastSyncedAt = null,
        isConfigured = false,
    )

    @Test
    fun totalPending_is_sum_of_all_pending_counts() {
        assertEquals(6, status(milestones = 1, growthLogs = 2, media = 3).totalPending)
    }

    @Test
    fun totalPending_is_zero_when_nothing_pending() {
        assertEquals(0, status().totalPending)
    }

    @Test
    fun totalPending_counts_only_milestones() {
        assertEquals(5, status(milestones = 5).totalPending)
    }

    @Test
    fun lastSyncedAt_is_preserved() {
        val ts = Instant.ofEpochMilli(1_000_000L)
        val s = SyncStatus(0, 0, 0, ts, true)
        assertEquals(ts, s.lastSyncedAt)
    }

    @Test
    fun isConfigured_false_when_not_set() {
        assertEquals(false, status().isConfigured)
    }
}
