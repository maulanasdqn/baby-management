package com.babyvault.android.data.engine

import com.babyvault.android.core.native.VaultEngineProvider
import com.babyvault.android.data.local.SyncConfigStore
import com.babyvault.android.data.mapper.toDomain
import com.babyvault.android.domain.model.SyncStatus
import com.babyvault.android.domain.repo.SyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EngineSyncRepository @Inject constructor(
    private val provider: VaultEngineProvider,
    private val configStore: SyncConfigStore,
) : SyncRepository {

    override suspend fun configureSyncServer(serverUrl: String, apiKey: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                configStore.save(serverUrl, apiKey)
                provider.engine.configureSyncServer(serverUrl, apiKey)
            }
        }

    override suspend fun syncNow(): Result<SyncStatus> =
        withContext(Dispatchers.IO) {
            runCatching { provider.engine.syncNow().toDomain() }
        }

    override suspend fun getStatus(): Result<SyncStatus> =
        withContext(Dispatchers.IO) {
            runCatching { provider.engine.getSyncStatus().toDomain() }
        }
}
