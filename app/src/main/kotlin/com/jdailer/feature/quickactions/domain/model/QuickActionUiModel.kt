package com.jdailer.feature.quickactions.domain.model

data class QuickActionUiModel(
    val actionId: String,
    val label: String,
    val type: SmartActionType,
    val enabled: Boolean = true,
    val requiresConsent: Boolean = false,
    val order: Int = 0
)
