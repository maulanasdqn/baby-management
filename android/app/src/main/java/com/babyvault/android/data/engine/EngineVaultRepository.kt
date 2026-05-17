package com.babyvault.android.data.engine
import com.babyvault.android.core.vault.VaultEngineProvider
import com.babyvault.android.domain.repo.VaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
class EngineVaultRepository @Inject constructor(
    private val provider: VaultEngineProvider,
) : VaultRepository {
    override suspend fun generateMasterKey(): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching { provider.engine.generateMasterKey() }
    }
    override suspend fun unlock(rawKey: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { provider.engine.unlock(rawKey) }
    }
}
