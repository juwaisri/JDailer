package com.jdailer.feature.integrations.email.domain.model

data class EmailRouteProfile(
    val canCompose: Boolean,
    val selectedPackage: String?,
    val includeBcc: Boolean,
    val reason: String
)
