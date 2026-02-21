package com.jdailer.feature.quickactions.domain.model

enum class SmartActionType {
    CALL,
    MESSAGE,
    EMAIL,
    APP,
    BLOCK,
    NOTE,
    SHARE
}

data class SmartAction(
    val type: SmartActionType,
    val actionId: String,
    val label: String,
    val sortOrder: Int
)
