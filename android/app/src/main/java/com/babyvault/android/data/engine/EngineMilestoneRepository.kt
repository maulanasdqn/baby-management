package com.babyvault.android.data.engine

import com.babyvault.android.core.native.VaultEngineProvider
import com.babyvault.android.data.mapper.toDomain
import com.babyvault.android.domain.model.Milestone
import com.babyvault.android.domain.repo.MilestoneRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class EngineMilestoneRepository @Inject constructor(
    private val provider: VaultEngineProvider,
) : MilestoneRepository {

    override suspend fun create(
        title: String,
        description: String,
        occurredAt: Instant,
    ): Result<Milestone> = withContext(Dispatchers.IO) {
        runCatching {
            provider.engine.createMilestone(title, description, occurredAt.toEpochMilli()).toDomain()
        }
    }

    override suspend fun list(limit: Int, offset: Int): Result<List<Milestone>> =
        withContext(Dispatchers.IO) {
            runCatching {
                provider.engine.listMilestones(limit.toUInt(), offset.toUInt()).map { it.toDomain() }
            }
        }

    override suspend fun getById(id: String): Result<Milestone> = withContext(Dispatchers.IO) {
        runCatching { provider.engine.getMilestone(id).toDomain() }
    }

    override suspend fun delete(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { provider.engine.deleteMilestone(id) }
    }
}
