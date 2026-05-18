package com.babyvault.android.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant

class InstantConverter {
    @TypeConverter
    fun fromEpochMilli(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun toEpochMilli(value: Instant?): Long? = value?.toEpochMilli()
}

@Database(
    entities = [
        FeedLogEntity::class,
        SleepLogEntity::class,
        DiaperLogEntity::class,
        GrowthLogEntity::class,
        MilestoneEntity::class,
        MediaItemEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun feedDao(): FeedDao
    abstract fun sleepDao(): SleepDao
    abstract fun diaperDao(): DiaperDao
    abstract fun growthDao(): GrowthDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun mediaDao(): MediaDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "babyvault.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
