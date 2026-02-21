package com.jdailer.feature.integrations.signal.domain.model

data class SignalRouteProfile(
    val canLaunch: Boolean,
    val selectedPackage: String?,
    val supportsCall: Boolean,
    val reason: String
)
