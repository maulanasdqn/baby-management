package com.babyvault.android.domain.repo

import com.babyvault.android.domain.model.GrowthLog
import com.babyvault.android.domain.model.MediaItem
import com.babyvault.android.domain.model.Milestone
import java.time.Instant

interface MilestoneRepository {
    suspend fun create(title: String, description: String, occurredAt: Instant): Result<Milestone>
    suspend fun list(limit: Int, offset: Int): Result<List<Milestone>>
    suspend fun getById(id: String): Result<Milestone>
    suspend fun delete(id: String): Result<Unit>
}

interface GrowthRepository {
    suspend fun log(
        weightGrams: Int?,
        heightMm: Int?,
        notes: String,
        loggedAt: Instant,
    ): Result<GrowthLog>
    suspend fun listByRange(from: Instant, to: Instant): Result<List<GrowthLog>>
}

interface MediaRepository {
    suspend fun store(title: String, plaintextBytes: ByteArray): Result<MediaItem>
    suspend fun read(id: String): Result<ByteArray>
    suspend fun list(limit: Int, offset: Int): Result<List<MediaItem>>
}

interface VaultRepository {
    suspend fun generateMasterKey(): Result<ByteArray>
    suspend fun unlock(rawKey: ByteArray): Result<Unit>
}
