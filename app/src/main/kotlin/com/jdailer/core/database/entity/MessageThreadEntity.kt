package com.jdailer.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jdailer.core.database.enums.HistorySource

@Entity(
    tableName = "message_threads",
    indices = [
        Index("source"),
        Index("lastMessageAt"),
        Index("contactId"),
        Index("normalizedAddress"),
        Index("isRcs")
    ]
)
data class MessageThreadEntity(
    @PrimaryKey
    val threadId: String,
    val source: HistorySource,
    val contactId: Long?,
    val normalizedAddress: String?,
    val title: String?,
    val snippet: String?,
    val lastMessageAt: Long,
    val unreadCount: Int = 0,
    val isRcs: Boolean = false,
    val metadata: String? = null,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
)
