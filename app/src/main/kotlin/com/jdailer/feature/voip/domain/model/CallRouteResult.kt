package com.jdailer.feature.voip.domain.model

enum class CallRouteType {
    TELECOM,
    FALLBACK_DIAL,
    FAILED
}

data class CallRouteResult(
    val number: String,
    val routeType: CallRouteType,
    val usedSipUri: Boolean,
    val message: String? = null
) {
    val isRouted: Boolean = routeType != CallRouteType.FAILED
}

