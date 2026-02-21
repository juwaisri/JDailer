package com.jdailer.feature.quickactions.domain.model

import com.jdailer.feature.contacts.domain.model.UnifiedContact
import com.jdailer.feature.quickactions.domain.model.SmartAction

data class SmartContactCardState(
    val contact: UnifiedContact,
    val actions: List<SmartAction>,
    val tags: List<String>,
    val latestNote: String?,
    val isQuickActionAvailable: Boolean
)
