package com.babyvault.android.core.vault

import com.babyvault.core.VaultEngine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultEngineProvider @Inject constructor() {
    lateinit var engine: VaultEngine
        private set

    fun initialize(dbPath: String, storageDir: String) {
        engine = VaultEngine(dbPath, storageDir)
    }

    val isInitialized: Boolean get() = ::engine.isInitialized
}
