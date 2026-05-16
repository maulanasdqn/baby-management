package com.babyvault.android.domain.usecase

import com.babyvault.android.domain.repo.VaultRepository
import javax.inject.Inject

class GenerateMasterKeyUseCase @Inject constructor(private val repo: VaultRepository) {
    suspend operator fun invoke(): Result<ByteArray> = repo.generateMasterKey()
}

class UnlockVaultUseCase @Inject constructor(private val repo: VaultRepository) {
    suspend operator fun invoke(rawKey: ByteArray): Result<Unit> = repo.unlock(rawKey)
}
