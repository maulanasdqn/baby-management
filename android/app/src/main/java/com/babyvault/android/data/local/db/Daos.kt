package com.babyvault.android.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FeedLogEntity)

    @Query("DELETE FROM feed_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM feed_logs WHERE loggedAt >= :from AND loggedAt <= :to ORDER BY loggedAt DESC")
    fun flowByRange(from: Long, to: Long): Flow<List<FeedLogEntity>>
}

@Dao
interface SleepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SleepLogEntity)

    @Query("DELETE FROM sleep_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM sleep_logs WHERE startTime >= :from AND startTime <= :to ORDER BY startTime DESC")
    fun flowByRange(from: Long, to: Long): Flow<List<SleepLogEntity>>
}

@Dao
interface DiaperDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DiaperLogEntity)

    @Query("DELETE FROM diaper_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM diaper_logs WHERE loggedAt >= :from AND loggedAt <= :to ORDER BY loggedAt DESC")
    fun flowByRange(from: Long, to: Long): Flow<List<DiaperLogEntity>>
}

@Dao
interface GrowthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GrowthLogEntity)

    @Query("DELETE FROM growth_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM growth_logs ORDER BY loggedAt DESC")
    fun flowAll(): Flow<List<GrowthLogEntity>>
}

@Dao
interface MilestoneDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MilestoneEntity)

    @Query("DELETE FROM milestones WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM milestones ORDER BY occurredAt DESC LIMIT :limit")
    fun flowAll(limit: Int): Flow<List<MilestoneEntity>>

    @Query("SELECT COUNT(*) FROM milestones")
    fun flowCount(): Flow<Int>
}

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MediaItemEntity)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM media_items ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    fun flowAll(limit: Int, offset: Int): Flow<List<MediaItemEntity>>
}
