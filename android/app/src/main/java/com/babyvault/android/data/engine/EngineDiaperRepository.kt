package com.babyvault.android.data.engine
import com.babyvault.android.core.vault.VaultEngineProvider
import com.babyvault.android.domain.model.DiaperLog
import com.babyvault.android.domain.model.DiaperType
import com.babyvault.android.domain.repo.DiaperRepository
import com.babyvault.core.DiaperTypeDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
class EngineDiaperRepository @Inject constructor(
    private val provider: VaultEngineProvider,
) : DiaperRepository {
    override suspend fun logDiaper(diaperType: DiaperType, notes: String): Result<DiaperLog> =
        withContext(Dispatchers.IO) {
            runCatching {
                provider.engine.logDiaper(
                    when (diaperType) {
                        DiaperType.WET -> DiaperTypeDto.WET
                        DiaperType.DIRTY -> DiaperTypeDto.DIRTY
                        DiaperType.BOTH -> DiaperTypeDto.BOTH
                    },
                    notes,
                    System.currentTimeMillis(),
                ).toDomain()
            }
        }
    override suspend fun listByRange(fromMillis: Long, toMillis: Long): Result<List<DiaperLog>> =
        withContext(Dispatchers.IO) {
            runCatching { provider.engine.listDiaperByRange(fromMillis, toMillis).map { it.toDomain() } }
        }
    override suspend fun delete(id: String): Result<Unit> =
        withContext(Dispatchers.IO) { runCatching { provider.engine.deleteDiaper(id) } }
}
private fun com.babyvault.core.DiaperLogDto.toDomain() = DiaperLog(
    id = id,
    diaperType = when (diaperType) {
        DiaperTypeDto.WET -> DiaperType.WET
        DiaperTypeDto.DIRTY -> DiaperType.DIRTY
        DiaperTypeDto.BOTH -> DiaperType.BOTH
    },
    notes = notes,
    loggedAt = Instant.ofEpochMilli(loggedAtMillis),
)
