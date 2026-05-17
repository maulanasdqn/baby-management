package com.babyvault.android.domain.usecase
import com.babyvault.android.domain.model.GrowthLog
import com.babyvault.android.domain.repo.GrowthRepository
import java.time.Instant
import javax.inject.Inject
class LogGrowthUseCase @Inject constructor(private val repo: GrowthRepository) {
    suspend operator fun invoke(
        weightGrams: Int?,
        heightMm: Int?,
        notes: String,
        loggedAt: Instant = Instant.now(),
    ): Result<GrowthLog> = repo.log(weightGrams, heightMm, notes, loggedAt)
}
class ListGrowthByRangeUseCase @Inject constructor(private val repo: GrowthRepository) {
    suspend operator fun invoke(from: Instant, to: Instant): Result<List<GrowthLog>> =
        repo.listByRange(from, to)
}
