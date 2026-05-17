package com.babyvault.android.domain.usecase
import com.babyvault.android.domain.model.DiaperLog
import com.babyvault.android.domain.model.DiaperType
import com.babyvault.android.domain.repo.DiaperRepository
import javax.inject.Inject
class LogDiaperUseCase @Inject constructor(private val repo: DiaperRepository) {
    suspend operator fun invoke(diaperType: DiaperType, notes: String): Result<DiaperLog> =
        repo.logDiaper(diaperType, notes)
}
class ListDiaperByRangeUseCase @Inject constructor(private val repo: DiaperRepository) {
    suspend operator fun invoke(fromMillis: Long, toMillis: Long): Result<List<DiaperLog>> =
        repo.listByRange(fromMillis, toMillis)
}
class DeleteDiaperUseCase @Inject constructor(private val repo: DiaperRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repo.delete(id)
}
