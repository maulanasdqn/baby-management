package com.babyvault.android.domain.repo
import com.babyvault.android.domain.model.DiaperLog
import com.babyvault.android.domain.model.DiaperType
import com.babyvault.android.domain.model.FeedLog
import com.babyvault.android.domain.model.FeedType
import com.babyvault.android.domain.model.GrowthLog
import com.babyvault.android.domain.model.MediaItem
import com.babyvault.android.domain.model.Milestone
import com.babyvault.android.domain.model.SleepLog
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
interface SyncRepository {
    suspend fun configureSyncServer(serverUrl: String, apiKey: String): Result<Unit>
    suspend fun syncNow(): Result<com.babyvault.android.domain.model.SyncStatus>
    suspend fun getStatus(): Result<com.babyvault.android.domain.model.SyncStatus>
}
interface FeedRepository {
    suspend fun logFeed(feedType: FeedType, amountMl: Int?, durationMinutes: Int?, side: String?, notes: String): Result<FeedLog>
    suspend fun listByRange(fromMillis: Long, toMillis: Long): Result<List<FeedLog>>
    suspend fun delete(id: String): Result<Unit>
}
interface SleepRepository {
    suspend fun logSleep(startTimeMillis: Long, endTimeMillis: Long, notes: String): Result<SleepLog>
    suspend fun listByRange(fromMillis: Long, toMillis: Long): Result<List<SleepLog>>
    suspend fun delete(id: String): Result<Unit>
}
interface DiaperRepository {
    suspend fun logDiaper(diaperType: DiaperType, notes: String): Result<DiaperLog>
    suspend fun listByRange(fromMillis: Long, toMillis: Long): Result<List<DiaperLog>>
    suspend fun delete(id: String): Result<Unit>
}
