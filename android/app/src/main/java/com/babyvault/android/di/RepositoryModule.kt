package com.babyvault.android.di
import com.babyvault.android.data.engine.EngineDiaperRepository
import com.babyvault.android.data.engine.EngineFeedRepository
import com.babyvault.android.data.engine.EngineGrowthRepository
import com.babyvault.android.data.engine.EngineMediaRepository
import com.babyvault.android.data.engine.EngineMilestoneRepository
import com.babyvault.android.data.engine.EngineSleepRepository
import com.babyvault.android.data.engine.EngineSyncRepository
import com.babyvault.android.data.engine.EngineVaultRepository
import com.babyvault.android.domain.repo.DiaperRepository
import com.babyvault.android.domain.repo.FeedRepository
import com.babyvault.android.domain.repo.GrowthRepository
import com.babyvault.android.domain.repo.MediaRepository
import com.babyvault.android.domain.repo.MilestoneRepository
import com.babyvault.android.domain.repo.SleepRepository
import com.babyvault.android.domain.repo.SyncRepository
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
    @Binds @Singleton abstract fun bindSync(impl: EngineSyncRepository): SyncRepository
    @Binds @Singleton abstract fun feedRepo(impl: EngineFeedRepository): FeedRepository
    @Binds @Singleton abstract fun sleepRepo(impl: EngineSleepRepository): SleepRepository
    @Binds @Singleton abstract fun diaperRepo(impl: EngineDiaperRepository): DiaperRepository
}
