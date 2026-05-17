package com.babyvault.android.domain.usecase
import com.babyvault.android.domain.model.SyncStatus
import com.babyvault.android.domain.repo.SyncRepository
import javax.inject.Inject
class ConfigureSyncServerUseCase @Inject constructor(private val repo: SyncRepository) {
    suspend operator fun invoke(serverUrl: String, apiKey: String): Result<Unit> =
        repo.configureSyncServer(serverUrl, apiKey)
}
class SyncNowUseCase @Inject constructor(private val repo: SyncRepository) {
    suspend operator fun invoke(): Result<SyncStatus> = repo.syncNow()
}
class GetSyncStatusUseCase @Inject constructor(private val repo: SyncRepository) {
    suspend operator fun invoke(): Result<SyncStatus> = repo.getStatus()
}
