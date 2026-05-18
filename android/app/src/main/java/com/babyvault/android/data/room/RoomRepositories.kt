package com.babyvault.android.data.room

import android.content.Context
import com.babyvault.android.data.local.db.DiaperDao
import com.babyvault.android.data.local.db.DiaperLogEntity
import com.babyvault.android.data.local.db.FeedDao
import com.babyvault.android.data.local.db.FeedLogEntity
import com.babyvault.android.data.local.db.GrowthDao
import com.babyvault.android.data.local.db.GrowthLogEntity
import com.babyvault.android.data.local.db.MediaDao
import com.babyvault.android.data.local.db.MediaItemEntity
import com.babyvault.android.data.local.db.MilestoneDao
import com.babyvault.android.data.local.db.MilestoneEntity
import com.babyvault.android.data.local.db.SleepDao
import com.babyvault.android.data.local.db.SleepLogEntity
import com.babyvault.android.domain.model.DiaperLog
import com.babyvault.android.domain.model.DiaperType
import com.babyvault.android.domain.model.FeedLog
import com.babyvault.android.domain.model.FeedType
import com.babyvault.android.domain.model.GrowthLog
import com.babyvault.android.domain.model.MediaItem
import com.babyvault.android.domain.model.Milestone
import com.babyvault.android.domain.model.SleepLog
import com.babyvault.android.domain.repo.DiaperRepository
import com.babyvault.android.domain.repo.FeedRepository
import com.babyvault.android.domain.repo.GrowthRepository
import com.babyvault.android.domain.repo.MediaRepository
import com.babyvault.android.domain.repo.MilestoneRepository
import com.babyvault.android.domain.repo.SleepRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// Feed
// ---------------------------------------------------------------------------

@Singleton
class RoomFeedRepository @Inject constructor(
    private val dao: FeedDao,
) : FeedRepository {

    override suspend fun logFeed(
        feedType: FeedType,
        amountMl: Int?,
        durationMinutes: Int?,
        side: String?,
        notes: String,
    ): Result<FeedLog> = runCatching {
        val now = Instant.now()
        val entity = FeedLogEntity(
            id = UUID.randomUUID().toString(),
            feedType = feedType.name,
            amountMl = amountMl,
            durationMinutes = durationMinutes,
            side = side,
            notes = notes,
            loggedAt = now.toEpochMilli(),
        )
        dao.insert(entity)
        entity.toDomain()
    }

    override suspend fun listByRange(fromMillis: Long, toMillis: Long): Result<List<FeedLog>> =
        runCatching {
            dao.flowByRange(fromMillis, toMillis).first().map { it.toDomain() }
        }

    override suspend fun delete(id: String): Result<Unit> = runCatching { dao.deleteById(id) }

    private fun FeedLogEntity.toDomain() = FeedLog(
        id = id,
        feedType = FeedType.valueOf(feedType),
        amountMl = amountMl,
        durationMinutes = durationMinutes,
        side = side,
        notes = notes,
        loggedAt = Instant.ofEpochMilli(loggedAt),
    )
}

// ---------------------------------------------------------------------------
// Sleep
// ---------------------------------------------------------------------------

@Singleton
class RoomSleepRepository @Inject constructor(
    private val dao: SleepDao,
) : SleepRepository {

    override suspend fun logSleep(
        startTimeMillis: Long,
        endTimeMillis: Long,
        notes: String,
    ): Result<SleepLog> = runCatching {
        val durationMinutes = (endTimeMillis - startTimeMillis) / 60_000L
        val entity = SleepLogEntity(
            id = UUID.randomUUID().toString(),
            startTime = startTimeMillis,
            endTime = endTimeMillis,
            notes = notes,
            durationMinutes = durationMinutes,
        )
        dao.insert(entity)
        entity.toDomain()
    }

    override suspend fun listByRange(fromMillis: Long, toMillis: Long): Result<List<SleepLog>> =
        runCatching {
            dao.flowByRange(fromMillis, toMillis).first().map { it.toDomain() }
        }

    override suspend fun delete(id: String): Result<Unit> = runCatching { dao.deleteById(id) }

    private fun SleepLogEntity.toDomain() = SleepLog(
        id = id,
        startTime = Instant.ofEpochMilli(startTime),
        endTime = Instant.ofEpochMilli(endTime),
        notes = notes,
        durationMinutes = durationMinutes,
    )
}

// ---------------------------------------------------------------------------
// Diaper
// ---------------------------------------------------------------------------

@Singleton
class RoomDiaperRepository @Inject constructor(
    private val dao: DiaperDao,
) : DiaperRepository {

    override suspend fun logDiaper(diaperType: DiaperType, notes: String): Result<DiaperLog> =
        runCatching {
            val now = Instant.now()
            val entity = DiaperLogEntity(
                id = UUID.randomUUID().toString(),
                diaperType = diaperType.name,
                notes = notes,
                loggedAt = now.toEpochMilli(),
            )
            dao.insert(entity)
            entity.toDomain()
        }

    override suspend fun listByRange(fromMillis: Long, toMillis: Long): Result<List<DiaperLog>> =
        runCatching {
            dao.flowByRange(fromMillis, toMillis).first().map { it.toDomain() }
        }

    override suspend fun delete(id: String): Result<Unit> = runCatching { dao.deleteById(id) }

    private fun DiaperLogEntity.toDomain() = DiaperLog(
        id = id,
        diaperType = DiaperType.valueOf(diaperType),
        notes = notes,
        loggedAt = Instant.ofEpochMilli(loggedAt),
    )
}

// ---------------------------------------------------------------------------
// Growth
// ---------------------------------------------------------------------------

@Singleton
class RoomGrowthRepository @Inject constructor(
    private val dao: GrowthDao,
) : GrowthRepository {

    override suspend fun log(
        weightGrams: Int?,
        heightMm: Int?,
        notes: String,
        loggedAt: Instant,
    ): Result<GrowthLog> = runCatching {
        val entity = GrowthLogEntity(
            id = UUID.randomUUID().toString(),
            weightGrams = weightGrams,
            heightMm = heightMm,
            notes = notes,
            loggedAt = loggedAt.toEpochMilli(),
        )
        dao.insert(entity)
        entity.toDomain()
    }

    override suspend fun listByRange(from: Instant, to: Instant): Result<List<GrowthLog>> =
        runCatching {
            dao.flowAll().first()
                .filter { it.loggedAt in from.toEpochMilli()..to.toEpochMilli() }
                .map { it.toDomain() }
        }

    override suspend fun delete(id: String): Result<Unit> = runCatching { dao.deleteById(id) }

    private fun GrowthLogEntity.toDomain() = GrowthLog(
        id = id,
        weightGrams = weightGrams,
        heightMm = heightMm,
        notes = notes,
        loggedAt = Instant.ofEpochMilli(loggedAt),
    )
}

// ---------------------------------------------------------------------------
// Milestone
// ---------------------------------------------------------------------------

@Singleton
class RoomMilestoneRepository @Inject constructor(
    private val dao: MilestoneDao,
) : MilestoneRepository {

    override suspend fun create(
        title: String,
        description: String,
        occurredAt: Instant,
    ): Result<Milestone> = runCatching {
        val now = Instant.now()
        val entity = MilestoneEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            occurredAt = occurredAt.toEpochMilli(),
            createdAt = now.toEpochMilli(),
        )
        dao.insert(entity)
        entity.toDomain()
    }

    override suspend fun list(limit: Int, offset: Int): Result<List<Milestone>> = runCatching {
        dao.flowAll(limit = Int.MAX_VALUE).first()
            .drop(offset)
            .take(limit)
            .map { it.toDomain() }
    }

    override suspend fun getById(id: String): Result<Milestone> = runCatching {
        dao.flowAll(limit = Int.MAX_VALUE).first()
            .firstOrNull { it.id == id }
            ?.toDomain()
            ?: error("Milestone $id not found")
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching { dao.deleteById(id) }

    private fun MilestoneEntity.toDomain() = Milestone(
        id = id,
        title = title,
        description = description,
        occurredAt = Instant.ofEpochMilli(occurredAt),
        createdAt = Instant.ofEpochMilli(createdAt),
    )
}

// ---------------------------------------------------------------------------
// Media
// ---------------------------------------------------------------------------

@Singleton
class RoomMediaRepository @Inject constructor(
    private val dao: MediaDao,
    @ApplicationContext private val context: Context,
) : MediaRepository {

    private val mediaDir: File get() = File(context.filesDir, "media").also { it.mkdirs() }

    override suspend fun store(title: String, plaintextBytes: ByteArray): Result<MediaItem> =
        runCatching {
            val id = UUID.randomUUID().toString()
            val file = File(mediaDir, id)
            file.writeBytes(plaintextBytes)
            val now = Instant.now()
            val entity = MediaItemEntity(
                id = id,
                title = title,
                encryptedPath = file.absolutePath,
                sizeBytes = plaintextBytes.size.toLong(),
                createdAt = now.toEpochMilli(),
            )
            dao.insert(entity)
            entity.toDomain()
        }

    override suspend fun read(id: String): Result<ByteArray> = runCatching {
        val entities = dao.flowAll(limit = Int.MAX_VALUE, offset = 0).first()
        val entity = entities.firstOrNull { it.id == id }
            ?: error("MediaItem $id not found")
        File(entity.encryptedPath).readBytes()
    }

    override suspend fun list(limit: Int, offset: Int): Result<List<MediaItem>> = runCatching {
        dao.flowAll(limit = limit, offset = offset).first().map { it.toDomain() }
    }

    private fun MediaItemEntity.toDomain() = MediaItem(
        id = id,
        title = title,
        encryptedPath = encryptedPath,
        sizeBytes = sizeBytes,
        createdAt = Instant.ofEpochMilli(createdAt),
    )
}
