package com.babyvault.android.domain.usecase
import com.babyvault.android.domain.model.MediaItem
import com.babyvault.android.domain.repo.MediaRepository
import javax.inject.Inject
class StoreMediaUseCase @Inject constructor(private val repo: MediaRepository) {
    suspend operator fun invoke(title: String, plaintextBytes: ByteArray): Result<MediaItem> =
        repo.store(title, plaintextBytes)
}
class ReadMediaUseCase @Inject constructor(private val repo: MediaRepository) {
    suspend operator fun invoke(id: String): Result<ByteArray> = repo.read(id)
}
class ListMediaUseCase @Inject constructor(private val repo: MediaRepository) {
    suspend operator fun invoke(limit: Int = 20, offset: Int = 0): Result<List<MediaItem>> =
        repo.list(limit, offset)
}
