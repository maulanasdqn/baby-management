package com.babyvault.android.di

import android.content.Context
import com.babyvault.android.core.vault.VaultEngineProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideVaultEngineProvider(
        @ApplicationContext context: Context,
    ): VaultEngineProvider {
        val dbPath = File(context.filesDir, "vault.db").absolutePath
        val storageDir = File(context.filesDir, "media").also { it.mkdirs() }.absolutePath
        return VaultEngineProvider().also { it.initialize(dbPath, storageDir) }
    }
}
