package com.jdailer.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contact_tags",
    indices = [Index("contactId"), Index("tag"), Index("value")]
)
data class ContactTagEntity(
    @PrimaryKey(autoGenerate = true)
    val tagId: Long = 0L,
    val contactId: Long,
    val tag: String,
    val value: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
