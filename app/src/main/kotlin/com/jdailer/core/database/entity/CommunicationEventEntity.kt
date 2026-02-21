package com.jdailer.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jdailer.core.database.enums.HistoryDirection
import com.jdailer.core.database.enums.HistorySource

@Entity(
    tableName = "communication_events",
    indices = [
        Index("timestamp"),
        Index("source"),
        Index("threadId"),
        Index("contactId"),
        Index("direction")
    ]
)
data class CommunicationEventEntity(
    @PrimaryKey
    val id: String,
    val contactId: Long?,
    val threadId: String,
    val source: HistorySource,
    val direction: HistoryDirection,
    val timestamp: Long,
    val snippet: String?,
    val read: Boolean = false,
    val metadata: String? = null,
    val isPinned: Boolean = false,
    val unreadCount: Int = 0,
)
