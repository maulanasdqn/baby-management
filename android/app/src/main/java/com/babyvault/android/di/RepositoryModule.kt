package com.babyvault.android.di

import com.babyvault.android.data.room.RoomDiaperRepository
import com.babyvault.android.data.room.RoomFeedRepository
import com.babyvault.android.data.room.RoomGrowthRepository
import com.babyvault.android.data.room.RoomMediaRepository
import com.babyvault.android.data.room.RoomMilestoneRepository
import com.babyvault.android.data.room.RoomSleepRepository
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
    @Binds @Singleton abstract fun bindMilestone(impl: RoomMilestoneRepository): MilestoneRepository
    @Binds @Singleton abstract fun bindGrowth(impl: RoomGrowthRepository): GrowthRepository
    @Binds @Singleton abstract fun bindMedia(impl: RoomMediaRepository): MediaRepository
    @Binds @Singleton abstract fun bindVault(impl: StubVaultRepository): VaultRepository
    @Binds @Singleton abstract fun bindSync(impl: StubSyncRepository): SyncRepository
    @Binds @Singleton abstract fun bindFeed(impl: RoomFeedRepository): FeedRepository
    @Binds @Singleton abstract fun bindSleep(impl: RoomSleepRepository): SleepRepository
    @Binds @Singleton abstract fun bindDiaper(impl: RoomDiaperRepository): DiaperRepository
}
