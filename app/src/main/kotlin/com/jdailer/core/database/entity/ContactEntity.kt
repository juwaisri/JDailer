package com.jdailer.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [
        Index("displayName"),
        Index("normalizedName"),
        Index("source")
    ]
)
data class ContactEntity(
    @PrimaryKey
    val contactId: Long,
    val displayName: String,
    val normalizedName: String,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val source: String = "local",
    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
