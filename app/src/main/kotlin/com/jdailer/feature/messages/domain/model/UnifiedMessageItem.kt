package com.jdailer.feature.messages.domain.model

import com.jdailer.core.database.enums.HistoryDirection
import com.jdailer.core.database.enums.HistorySource

data class UnifiedMessageItem(
    val itemId: String,
    val threadId: String,
    val source: HistorySource,
    val direction: HistoryDirection,
    val contactId: Long?,
    val address: String?,
    val body: String?,
    val mediaUri: String?,
    val timestamp: Long,
    val isRead: Boolean,
    val isRcs: Boolean
)
