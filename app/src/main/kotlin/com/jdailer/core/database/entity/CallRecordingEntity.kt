package com.jdailer.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_recordings",
    indices = [
        Index("callId"),
        Index("timestamp")
    ]
)
data class CallRecordingEntity(
    @PrimaryKey
    val recordingId: String,
    val callId: String,
    val timestamp: Long,
    val callerNumber: String?,
    val direction: String,
    val filePath: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val consented: Boolean,
)
