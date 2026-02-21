package com.jdailer.feature.history.domain.model

import com.jdailer.core.database.enums.HistoryDirection
import com.jdailer.core.database.enums.HistorySource

data class UnifiedHistoryItem(
    val id: String,
    val contactId: Long?,
    val threadId: String,
    val source: HistorySource,
    val direction: HistoryDirection,
    val timestamp: Long,
    val snippet: String?,
    val unreadCount: Int,
    val isRead: Boolean,
)
