package com.jdailer.feature.integrations.telegram.domain.model

data class TelegramRouteProfile(
    val canLaunch: Boolean,
    val selectedPackage: String?,
    val supportsVoice: Boolean,
    val isInstalled: Boolean,
    val reason: String
)
