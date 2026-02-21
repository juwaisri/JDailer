package com.jdailer.feature.messages.domain.model

import com.jdailer.core.database.enums.HistorySource

data class SmsMmsSyncResult(
    val syncedAt: Long,
    val syncedMessages: Int,
    val syncedThreads: Int,
    val sourceCounts: Map<HistorySource, Int>,
    val hasRcsCapability: Boolean,
    val isFullSync: Boolean
)

