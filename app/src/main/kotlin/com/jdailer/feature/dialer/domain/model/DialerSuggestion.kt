package com.jdailer.feature.dialer.domain.model

import com.jdailer.core.database.enums.HistorySource

data class DialerSuggestion(
    val contactId: Long,
    val displayName: String,
    val phoneNumber: String,
    val normalizedNumber: String,
    val isFavorite: Boolean,
    val score: Float,
    val source: HistorySource = HistorySource.CALL,
)
