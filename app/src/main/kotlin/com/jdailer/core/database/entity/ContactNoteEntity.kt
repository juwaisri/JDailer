package com.jdailer.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contact_notes",
    indices = [Index("contactId"), Index("createdAt"), Index("updatedAt")]
)
data class ContactNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val noteId: Long = 0L,
    val contactId: Long,
    val note: String,
    val createdByUser: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
