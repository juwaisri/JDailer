package com.jdailer.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spam_profiles")
data class SpamProfileEntity(
    @PrimaryKey
    val normalizedNumber: String,
    val confidenceScore: Int,
    val reason: String?,
    val shouldBlock: Boolean,
    val source: String,
    val updatedAt: Long = System.currentTimeMillis(),
)
