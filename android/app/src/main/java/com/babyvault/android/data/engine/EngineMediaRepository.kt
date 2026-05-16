package com.babyvault.android.data.engine

import com.babyvault.android.core.native.VaultEngineProvider
import com.babyvault.android.data.mapper.toDomain
import com.babyvault.android.domain.model.MediaItem
import com.babyvault.android.domain.repo.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EngineMediaRepository @Inject constructor(
    private val provider: VaultEngineProvider,
) : MediaRepository {

    override suspend fun store(title: String, plaintextBytes: ByteArray): Result<MediaItem> =
        withContext(Dispatchers.IO) {
            runCatching { provider.engine.storeMedia(title, plaintextBytes.toList()).toDomain() }
        }

    override suspend fun read(id: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching { provider.engine.readMedia(id).toByteArray() }
    }

    override suspend fun list(limit: Int, offset: Int): Result<List<MediaItem>> =
        withContext(Dispatchers.IO) {
            runCatching {
                provider.engine.listMedia(limit.toUInt(), offset.toUInt()).map { it.toDomain() }
            }
        }
}
