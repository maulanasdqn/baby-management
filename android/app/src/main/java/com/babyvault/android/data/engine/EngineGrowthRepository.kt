package com.babyvault.android.data.engine

import com.babyvault.android.core.native.VaultEngineProvider
import com.babyvault.android.data.mapper.toDomain
import com.babyvault.android.domain.model.GrowthLog
import com.babyvault.android.domain.repo.GrowthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class EngineGrowthRepository @Inject constructor(
    private val provider: VaultEngineProvider,
) : GrowthRepository {

    override suspend fun log(
        weightGrams: Int?,
        heightMm: Int?,
        notes: String,
        loggedAt: Instant,
    ): Result<GrowthLog> = withContext(Dispatchers.IO) {
        runCatching {
            provider.engine.logGrowth(
                weightGrams?.toUInt(),
                heightMm?.toUInt(),
                notes,
                loggedAt.toEpochMilli(),
            ).toDomain()
        }
    }

    override suspend fun listByRange(from: Instant, to: Instant): Result<List<GrowthLog>> =
        withContext(Dispatchers.IO) {
            runCatching {
                provider.engine.listGrowthByRange(from.toEpochMilli(), to.toEpochMilli())
                    .map { it.toDomain() }
            }
        }
}
