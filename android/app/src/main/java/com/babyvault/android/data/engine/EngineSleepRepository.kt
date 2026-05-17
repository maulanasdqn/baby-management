package com.babyvault.android.data.engine
import com.babyvault.android.core.vault.VaultEngineProvider
import com.babyvault.android.domain.model.SleepLog
import com.babyvault.android.domain.repo.SleepRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
class EngineSleepRepository @Inject constructor(
    private val provider: VaultEngineProvider,
) : SleepRepository {
    override suspend fun logSleep(startTimeMillis: Long, endTimeMillis: Long, notes: String): Result<SleepLog> =
        withContext(Dispatchers.IO) {
            runCatching { provider.engine.logSleep(startTimeMillis, endTimeMillis, notes).toDomain() }
        }
    override suspend fun listByRange(fromMillis: Long, toMillis: Long): Result<List<SleepLog>> =
        withContext(Dispatchers.IO) {
            runCatching { provider.engine.listSleepByRange(fromMillis, toMillis).map { it.toDomain() } }
        }
    override suspend fun delete(id: String): Result<Unit> =
        withContext(Dispatchers.IO) { runCatching { provider.engine.deleteSleep(id) } }
}
private fun com.babyvault.core.SleepLogDto.toDomain() = SleepLog(
    id = id,
    startTime = Instant.ofEpochMilli(startTimeMillis),
    endTime = Instant.ofEpochMilli(endTimeMillis),
    notes = notes,
    durationMinutes = durationMinutes,
)
