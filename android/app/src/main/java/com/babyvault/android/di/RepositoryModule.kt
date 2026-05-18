package com.babyvault.android.di

import com.babyvault.android.data.stub.StubDiaperRepository
import com.babyvault.android.data.stub.StubFeedRepository
import com.babyvault.android.data.stub.StubGrowthRepository
import com.babyvault.android.data.stub.StubMediaRepository
import com.babyvault.android.data.stub.StubMilestoneRepository
import com.babyvault.android.data.stub.StubSleepRepository
import com.babyvault.android.data.stub.StubSyncRepository
import com.babyvault.android.data.stub.StubVaultRepository
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
    @Binds @Singleton abstract fun bindMilestone(impl: StubMilestoneRepository): MilestoneRepository
    @Binds @Singleton abstract fun bindGrowth(impl: StubGrowthRepository): GrowthRepository
    @Binds @Singleton abstract fun bindMedia(impl: StubMediaRepository): MediaRepository
    @Binds @Singleton abstract fun bindVault(impl: StubVaultRepository): VaultRepository
    @Binds @Singleton abstract fun bindSync(impl: StubSyncRepository): SyncRepository
    @Binds @Singleton abstract fun bindFeed(impl: StubFeedRepository): FeedRepository
    @Binds @Singleton abstract fun bindSleep(impl: StubSleepRepository): SleepRepository
    @Binds @Singleton abstract fun bindDiaper(impl: StubDiaperRepository): DiaperRepository
}
