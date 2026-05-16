package com.babyvault.android.data.engine

import com.babyvault.android.core.vault.VaultEngineProvider
import com.babyvault.android.domain.model.FeedLog
import com.babyvault.android.domain.model.FeedType
import com.babyvault.android.domain.repo.FeedRepository
import com.babyvault.core.FeedTypeDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class EngineFeedRepository @Inject constructor(
    private val provider: VaultEngineProvider,
) : FeedRepository {
    override suspend fun logFeed(feedType: FeedType, amountMl: Int?, durationMinutes: Int?, side: String?, notes: String): Result<FeedLog> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dto = provider.engine.logFeed(
                    feedType.toDto(),
                    amountMl?.toUInt(),
                    durationMinutes?.toUInt(),
                    side,
                    notes,
                    System.currentTimeMillis(),
                )
                dto.toDomain()
            }
        }

    override suspend fun listByRange(fromMillis: Long, toMillis: Long): Result<List<FeedLog>> =
        withContext(Dispatchers.IO) {
            runCatching { provider.engine.listFeedByRange(fromMillis, toMillis).map { it.toDomain() } }
        }

    override suspend fun delete(id: String): Result<Unit> =
        withContext(Dispatchers.IO) { runCatching { provider.engine.deleteFeed(id) } }
}

private fun FeedType.toDto() = when (this) {
    FeedType.BREAST -> FeedTypeDto.BREAST
    FeedType.BOTTLE -> FeedTypeDto.BOTTLE
    FeedType.SOLID -> FeedTypeDto.SOLID
}

private fun com.babyvault.core.FeedLogDto.toDomain() = FeedLog(
    id = id,
    feedType = when (feedType) {
        FeedTypeDto.BREAST -> FeedType.BREAST
        FeedTypeDto.BOTTLE -> FeedType.BOTTLE
        FeedTypeDto.SOLID -> FeedType.SOLID
    },
    amountMl = amountMl?.toInt(),
    durationMinutes = durationMinutes?.toInt(),
    side = side,
    notes = notes,
    loggedAt = Instant.ofEpochMilli(loggedAtMillis),
)
