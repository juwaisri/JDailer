package com.jdailer.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "caller_lookup",
    indices = [Index("lastCheckedAt"), Index("isSpam"), Index("spamScore")]
)
data class CallerLookupEntity(
    @PrimaryKey
    val normalizedNumber: String,
    val displayName: String?,
    val city: String?,
    val carrier: String?,
    val isSpam: Boolean,
    val spamScore: Int,
    val reason: String?,
    val lastCheckedAt: Long,
    val isUserBlocked: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
