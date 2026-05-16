package com.babyvault.android.di

import com.babyvault.android.data.engine.EngineGrowthRepository
import com.babyvault.android.data.engine.EngineMediaRepository
import com.babyvault.android.data.engine.EngineMilestoneRepository
import com.babyvault.android.data.engine.EngineVaultRepository
import com.babyvault.android.domain.repo.GrowthRepository
import com.babyvault.android.domain.repo.MediaRepository
import com.babyvault.android.domain.repo.MilestoneRepository
import com.babyvault.android.domain.repo.VaultRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindMilestone(impl: EngineMilestoneRepository): MilestoneRepository
    @Binds @Singleton abstract fun bindGrowth(impl: EngineGrowthRepository): GrowthRepository
    @Binds @Singleton abstract fun bindMedia(impl: EngineMediaRepository): MediaRepository
    @Binds @Singleton abstract fun bindVault(impl: EngineVaultRepository): VaultRepository
}
