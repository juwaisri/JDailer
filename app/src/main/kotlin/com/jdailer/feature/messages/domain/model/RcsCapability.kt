package com.jdailer.feature.messages.domain.model

data class RcsCapability(
    val isRcsEnabledByDefaultSmsApp: Boolean,
    val isRcsAvailableAsCarrierService: Boolean,
    val defaultSmsPackageName: String?,
    val supportsRcsMmsFallback: Boolean,
    val rcsServicePackage: String?,
    val detectedAtMs: Long = System.currentTimeMillis()
)
