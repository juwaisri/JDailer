package com.jdailer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jdailer.core.database.entity.CallRecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallRecordingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(recording: CallRecordingEntity)

    @Query("SELECT * FROM call_recordings ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<CallRecordingEntity>>

    @Query(
        """
        UPDATE call_recordings
        SET durationMs = :durationMs,
            fileSizeBytes = :fileSizeBytes
        WHERE recordingId = :recordingId
        """
    )
    suspend fun updateDuration(recordingId: String, durationMs: Long, fileSizeBytes: Long)

    @Query("DELETE FROM call_recordings WHERE recordingId = :recordingId")
    suspend fun deleteRecording(recordingId: String)

    @Query("DELETE FROM call_recordings")
    suspend fun clearAll()
}
