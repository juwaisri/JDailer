package com.jdailer.feature.messages.domain.model

enum class MessageTransportMode {
    SMS,
    MMS,
    RCS
}

data class MessageDeliveryPolicy(
    val preferRcs: Boolean = true,
    val fallbackToSmsWhenRcsUnavailable: Boolean = true,
    val preferRcsForShortMessages: Boolean = true,
    val maxSmsCharacters: Int = 620,
    val mmsFallbackLength: Int = 1500,
    val requiresAttachmentAsMms: Boolean = true,
    val includeRcsCapabilityRefresh: Boolean = true,
    val redactMetadataOnSecureTransport: Boolean = true
)

data class MessageDeliveryDecision(
    val recipient: String,
    val mode: MessageTransportMode,
    val useFallbackMms: Boolean,
    val secureChannelPreferred: Boolean,
    val reason: String
)
