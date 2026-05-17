package com.babyvault.android.domain.usecase
import com.babyvault.android.domain.model.FeedLog
import com.babyvault.android.domain.model.FeedType
import com.babyvault.android.domain.repo.FeedRepository
import javax.inject.Inject
class LogFeedUseCase @Inject constructor(private val repo: FeedRepository) {
    suspend operator fun invoke(feedType: FeedType, amountMl: Int?, durationMinutes: Int?, side: String?, notes: String): Result<FeedLog> =
        repo.logFeed(feedType, amountMl, durationMinutes, side, notes)
}
class ListFeedByRangeUseCase @Inject constructor(private val repo: FeedRepository) {
    suspend operator fun invoke(fromMillis: Long, toMillis: Long): Result<List<FeedLog>> =
        repo.listByRange(fromMillis, toMillis)
}
class DeleteFeedUseCase @Inject constructor(private val repo: FeedRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repo.delete(id)
}
