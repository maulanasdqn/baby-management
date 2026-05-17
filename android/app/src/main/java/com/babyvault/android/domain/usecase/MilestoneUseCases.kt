package com.babyvault.android.domain.usecase
import com.babyvault.android.domain.model.Milestone
import com.babyvault.android.domain.repo.MilestoneRepository
import java.time.Instant
import javax.inject.Inject
class CreateMilestoneUseCase @Inject constructor(private val repo: MilestoneRepository) {
    suspend operator fun invoke(
        title: String,
        description: String,
        occurredAt: Instant,
    ): Result<Milestone> = repo.create(title, description, occurredAt)
}
class ListMilestonesUseCase @Inject constructor(private val repo: MilestoneRepository) {
    suspend operator fun invoke(limit: Int = 20, offset: Int = 0): Result<List<Milestone>> =
        repo.list(limit, offset)
}
class DeleteMilestoneUseCase @Inject constructor(private val repo: MilestoneRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repo.delete(id)
}
