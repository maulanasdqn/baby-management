package com.babyvault.android.domain.usecase
import com.babyvault.android.domain.model.SleepLog
import com.babyvault.android.domain.repo.SleepRepository
import javax.inject.Inject
class LogSleepUseCase @Inject constructor(private val repo: SleepRepository) {
    suspend operator fun invoke(startTimeMillis: Long, endTimeMillis: Long, notes: String): Result<SleepLog> =
        repo.logSleep(startTimeMillis, endTimeMillis, notes)
}
class ListSleepByRangeUseCase @Inject constructor(private val repo: SleepRepository) {
    suspend operator fun invoke(fromMillis: Long, toMillis: Long): Result<List<SleepLog>> =
        repo.listByRange(fromMillis, toMillis)
}
class DeleteSleepUseCase @Inject constructor(private val repo: SleepRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repo.delete(id)
}
