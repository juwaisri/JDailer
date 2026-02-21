package com.jdailer.feature.messages.domain.model

import com.jdailer.core.database.enums.HistorySource

data class UnifiedMessageThread(
    val threadId: String,
    val source: HistorySource,
    val contactId: Long?,
    val address: String?,
    val title: String?,
    val snippet: String?,
    val unreadCount: Int,
    val lastMessageAt: Long,
    val isRcs: Boolean,
    val metadata: MessageThreadMetadata = MessageThreadMetadata.Empty,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
)
