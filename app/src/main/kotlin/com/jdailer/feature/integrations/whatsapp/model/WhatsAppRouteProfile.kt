package com.jdailer.feature.integrations.whatsapp.model

data class WhatsAppRouteProfile(
    val canLaunch: Boolean,
    val useBusinessEndpoint: Boolean,
    val selectedPackage: String?,
    val canPlaceCall: Boolean = false,
    val reason: String
)
