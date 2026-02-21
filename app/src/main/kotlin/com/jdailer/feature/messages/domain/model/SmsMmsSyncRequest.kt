package com.jdailer.feature.messages.domain.model

data class SmsMmsSyncRequest(
    val limitPerType: Int = 250,
    val forceFromDevice: Boolean = true,
    val refreshRcsCapability: Boolean = true,
    val includeUnknownSources: Boolean = false
)
