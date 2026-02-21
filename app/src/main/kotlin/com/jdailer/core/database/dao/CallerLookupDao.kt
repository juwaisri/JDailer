package com.jdailer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jdailer.core.database.entity.CallerLookupEntity

@Dao
interface CallerLookupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: CallerLookupEntity)

    @Query("SELECT * FROM caller_lookup WHERE normalizedNumber = :normalizedNumber LIMIT 1")
    suspend fun getByNumber(normalizedNumber: String): CallerLookupEntity?

    @Query("SELECT * FROM caller_lookup WHERE isSpam = 1 ORDER BY spamScore DESC")
    suspend fun getSpamEntries(): List<CallerLookupEntity>

    @Query("UPDATE caller_lookup SET isUserBlocked = :blocked, updatedAt = :updatedAt WHERE normalizedNumber = :normalizedNumber")
    suspend fun updateUserBlock(normalizedNumber: String, blocked: Boolean, updatedAt: Long)

    @Query("SELECT * FROM caller_lookup ORDER BY lastCheckedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<CallerLookupEntity>
}
