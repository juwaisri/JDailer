package com.jdailer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jdailer.core.database.entity.SpamProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: SpamProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(profiles: List<SpamProfileEntity>)

    @Query("SELECT * FROM spam_profiles WHERE normalizedNumber = :normalizedNumber LIMIT 1")
    suspend fun getProfile(normalizedNumber: String): SpamProfileEntity?

    @Query("SELECT * FROM spam_profiles WHERE shouldBlock = 1")
    fun observeBlocklist(): Flow<List<SpamProfileEntity>>

    @Query("DELETE FROM spam_profiles WHERE normalizedNumber = :normalizedNumber")
    suspend fun deleteProfile(normalizedNumber: String)
}
