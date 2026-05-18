package com.babyvault.android.di

import android.content.Context
import com.babyvault.android.data.local.db.AppDatabase
import com.babyvault.android.data.local.db.DiaperDao
import com.babyvault.android.data.local.db.FeedDao
import com.babyvault.android.data.local.db.GrowthDao
import com.babyvault.android.data.local.db.MediaDao
import com.babyvault.android.data.local.db.MilestoneDao
import com.babyvault.android.data.local.db.SleepDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.create(context)

    @Provides
    @Singleton
    fun provideFeedDao(db: AppDatabase): FeedDao = db.feedDao()

    @Provides
    @Singleton
    fun provideSleepDao(db: AppDatabase): SleepDao = db.sleepDao()

    @Provides
    @Singleton
    fun provideDiaperDao(db: AppDatabase): DiaperDao = db.diaperDao()

    @Provides
    @Singleton
    fun provideGrowthDao(db: AppDatabase): GrowthDao = db.growthDao()

    @Provides
    @Singleton
    fun provideMilestoneDao(db: AppDatabase): MilestoneDao = db.milestoneDao()

    @Provides
    @Singleton
    fun provideMediaDao(db: AppDatabase): MediaDao = db.mediaDao()
}
