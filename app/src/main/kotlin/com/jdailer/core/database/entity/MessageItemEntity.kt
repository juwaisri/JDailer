package com.jdailer.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jdailer.core.database.enums.HistoryDirection
import com.jdailer.core.database.enums.HistorySource

@Entity(
    tableName = "message_items",
    indices = [
        Index("threadId"),
        Index("timestamp"),
        Index("source"),
        Index("direction"),
        Index("isRead"),
        Index("isRcs")
    ]
)
data class MessageItemEntity(
    @PrimaryKey
    val itemId: String,
    val threadId: String,
    val source: HistorySource,
    val direction: HistoryDirection,
    val contactId: Long?,
    val normalizedAddress: String?,
    val body: String?,
    val mediaUri: String?,
    val timestamp: Long,
    val isRead: Boolean,
    val isRcs: Boolean = false,
    val metadata: String? = null,
)
