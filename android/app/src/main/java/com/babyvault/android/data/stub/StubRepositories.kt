package com.babyvault.android.data.stub

import com.babyvault.android.domain.model.DiaperLog
import com.babyvault.android.domain.model.DiaperType
import com.babyvault.android.domain.model.FeedLog
import com.babyvault.android.domain.model.FeedType
import com.babyvault.android.domain.model.GrowthLog
import com.babyvault.android.domain.model.MediaItem
import com.babyvault.android.domain.model.Milestone
import com.babyvault.android.domain.model.SleepLog
import com.babyvault.android.domain.model.SyncStatus
import com.babyvault.android.domain.repo.DiaperRepository
import com.babyvault.android.domain.repo.FeedRepository
import com.babyvault.android.domain.repo.GrowthRepository
import com.babyvault.android.domain.repo.MediaRepository
import com.babyvault.android.domain.repo.MilestoneRepository
import com.babyvault.android.domain.repo.SleepRepository
import com.babyvault.android.domain.repo.SyncRepository
import com.babyvault.android.domain.repo.VaultRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StubMilestoneRepository @Inject constructor() : MilestoneRepository {
    private val store = mutableListOf<Milestone>()
    override suspend fun create(title: String, description: String, occurredAt: Instant): Result<Milestone> {
        val m = Milestone(id = UUID.randomUUID().toString(), title = title, description = description, occurredAt = occurredAt, createdAt = Instant.now())
        store.add(0, m)
        return Result.success(m)
    }
    override suspend fun list(limit: Int, offset: Int): Result<List<Milestone>> =
        Result.success(store.drop(offset).take(limit))
    override suspend fun getById(id: String): Result<Milestone> =
        store.find { it.id == id }?.let { Result.success(it) } ?: Result.failure(NoSuchElementException(id))
    override suspend fun delete(id: String): Result<Unit> {
        store.removeIf { it.id == id }
        return Result.success(Unit)
    }
}

@Singleton
class StubGrowthRepository @Inject constructor() : GrowthRepository {
    private val store = mutableListOf<GrowthLog>()
    override suspend fun log(weightGrams: Int?, heightMm: Int?, notes: String, loggedAt: Instant): Result<GrowthLog> {
        val g = GrowthLog(id = UUID.randomUUID().toString(), weightGrams = weightGrams, heightMm = heightMm, notes = notes, loggedAt = loggedAt)
        store.add(0, g)
        return Result.success(g)
    }
    override suspend fun listByRange(from: Instant, to: Instant): Result<List<GrowthLog>> =
        Result.success(store.filter { !it.loggedAt.isBefore(from) && !it.loggedAt.isAfter(to) })
    override suspend fun delete(id: String): Result<Unit> {
        store.removeIf { it.id == id }
        return Result.success(Unit)
    }
}

@Singleton
class StubMediaRepository @Inject constructor() : MediaRepository {
    private val store = mutableListOf<MediaItem>()
    private val blobs = mutableMapOf<String, ByteArray>()
    override suspend fun store(title: String, plaintextBytes: ByteArray): Result<MediaItem> {
        val item = MediaItem(id = UUID.randomUUID().toString(), title = title, encryptedPath = "", sizeBytes = plaintextBytes.size.toLong(), createdAt = Instant.now())
        store.add(0, item)
        blobs[item.id] = plaintextBytes
        return Result.success(item)
    }
    override suspend fun read(id: String): Result<ByteArray> =
        blobs[id]?.let { Result.success(it) } ?: Result.failure(NoSuchElementException(id))
    override suspend fun list(limit: Int, offset: Int): Result<List<MediaItem>> =
        Result.success(store.drop(offset).take(limit))
}

@Singleton
class StubVaultRepository @Inject constructor() : VaultRepository {
    override suspend fun generateMasterKey(): Result<ByteArray> = Result.success(ByteArray(32))
    override suspend fun unlock(rawKey: ByteArray): Result<Unit> = Result.success(Unit)
}

@Singleton
class StubSyncRepository @Inject constructor() : SyncRepository {
    override suspend fun configureSyncServer(serverUrl: String, apiKey: String): Result<Unit> = Result.success(Unit)
    override suspend fun syncNow(): Result<SyncStatus> = Result.success(empty())
    override suspend fun getStatus(): Result<SyncStatus> = Result.success(empty())
    private fun empty() = SyncStatus(0, 0, 0, null, false)
}

@Singleton
class StubFeedRepository @Inject constructor() : FeedRepository {
    private val store = mutableListOf<FeedLog>()
    override suspend fun logFeed(feedType: FeedType, amountMl: Int?, durationMinutes: Int?, side: String?, notes: String): Result<FeedLog> {
        val f = FeedLog(id = UUID.randomUUID().toString(), feedType = feedType, amountMl = amountMl, durationMinutes = durationMinutes, side = side, notes = notes, loggedAt = Instant.now())
        store.add(0, f)
        return Result.success(f)
    }
    override suspend fun listByRange(fromMillis: Long, toMillis: Long): Result<List<FeedLog>> =
        Result.success(store.filter { it.loggedAt.toEpochMilli() in fromMillis..toMillis })
    override suspend fun delete(id: String): Result<Unit> {
        store.removeIf { it.id == id }
        return Result.success(Unit)
    }
}

@Singleton
class StubSleepRepository @Inject constructor() : SleepRepository {
    private val store = mutableListOf<SleepLog>()
    override suspend fun logSleep(startTimeMillis: Long, endTimeMillis: Long, notes: String): Result<SleepLog> {
        val start = Instant.ofEpochMilli(startTimeMillis)
        val end = Instant.ofEpochMilli(endTimeMillis)
        val duration = (endTimeMillis - startTimeMillis) / 60_000
        val s = SleepLog(id = UUID.randomUUID().toString(), startTime = start, endTime = end, notes = notes, durationMinutes = duration)
        store.add(0, s)
        return Result.success(s)
    }
    override suspend fun listByRange(fromMillis: Long, toMillis: Long): Result<List<SleepLog>> =
        Result.success(store.filter { it.startTime.toEpochMilli() in fromMillis..toMillis })
    override suspend fun delete(id: String): Result<Unit> {
        store.removeIf { it.id == id }
        return Result.success(Unit)
    }
}

@Singleton
class StubDiaperRepository @Inject constructor() : DiaperRepository {
    private val store = mutableListOf<DiaperLog>()
    override suspend fun logDiaper(diaperType: DiaperType, notes: String): Result<DiaperLog> {
        val d = DiaperLog(id = UUID.randomUUID().toString(), diaperType = diaperType, notes = notes, loggedAt = Instant.now())
        store.add(0, d)
        return Result.success(d)
    }
    override suspend fun listByRange(fromMillis: Long, toMillis: Long): Result<List<DiaperLog>> =
        Result.success(store.filter { it.loggedAt.toEpochMilli() in fromMillis..toMillis })
    override suspend fun delete(id: String): Result<Unit> {
        store.removeIf { it.id == id }
        return Result.success(Unit)
    }
}
