package com.jdailer.feature.messages.domain.usecase

import com.jdailer.feature.messages.domain.model.MessageDeliveryDecision
import com.jdailer.feature.messages.domain.model.MessageDeliveryPolicy
import com.jdailer.feature.messages.domain.model.MessageTransportMode
import com.jdailer.feature.messages.domain.repository.MessageCapabilityRepository

class ResolveMessageDeliveryUseCase(
    private val messageCapabilityRepository: MessageCapabilityRepository,
    private val policy: MessageDeliveryPolicy = MessageDeliveryPolicy()
) {
    suspend operator fun invoke(
        rawRecipient: String,
        messageText: String?,
        attachmentUris: List<String> = emptyList()
    ): MessageDeliveryDecision {
        val normalizedRecipient = rawRecipient.filter { it.isDigit() || it == '+' }.ifBlank { rawRecipient.trim() }

        if (normalizedRecipient.isBlank()) {
            return MessageDeliveryDecision(
                recipient = rawRecipient,
                mode = MessageTransportMode.SMS,
                useFallbackMms = true,
                secureChannelPreferred = false,
                reason = "Invalid recipient"
            )
        }

        val attachmentCount = attachmentUris.size
        val messageLength = messageText.orEmpty().trim().length
        val isLengthOverSmsLimit = messageLength > policy.maxSmsCharacters
        if (isLengthOverSmsLimit) {
            return MessageDeliveryDecision(
                recipient = normalizedRecipient,
                mode = MessageTransportMode.MMS,
                useFallbackMms = true,
                secureChannelPreferred = false,
                reason = "Message exceeds SMS threshold"
            )
        }

        if (policy.requiresAttachmentAsMms && attachmentCount > 0) {
            return MessageDeliveryDecision(
                recipient = normalizedRecipient,
                mode = MessageTransportMode.MMS,
                useFallbackMms = true,
                secureChannelPreferred = false,
                reason = "Attachments present"
            )
        }

        if (messageLength > policy.mmsFallbackLength) {
            return MessageDeliveryDecision(
                recipient = normalizedRecipient,
                mode = MessageTransportMode.MMS,
                useFallbackMms = true,
                secureChannelPreferred = false,
                reason = "Long-form thread body requires MMS"
            )
        }

        val rcsCapability = runCatching { messageCapabilityRepository.getRcsCapability() }.getOrNull()
        val isRcsUsable = policy.preferRcs &&
            (rcsCapability?.isRcsEnabledByDefaultSmsApp == true || rcsCapability?.isRcsAvailableAsCarrierService == true)

        return if (policy.preferRcsForShortMessages && isRcsUsable && policy.fallbackToSmsWhenRcsUnavailable) {
            MessageDeliveryDecision(
                recipient = normalizedRecipient,
                mode = MessageTransportMode.RCS,
                useFallbackMms = false,
                secureChannelPreferred = true,
                reason = if (rcsCapability?.supportsRcsMmsFallback == true) {
                    "Default SMS/RCS app reports capability"
                } else {
                    "Carrier transport supports RCS"
                }
            )
        } else {
            MessageDeliveryDecision(
                recipient = normalizedRecipient,
                mode = MessageTransportMode.SMS,
                useFallbackMms = policy.fallbackToSmsWhenRcsUnavailable.not(),
                secureChannelPreferred = false,
                reason = if (isRcsUsable) "Policy prefers SMS fallback" else "RCS not available"
            )
        }
    }
}
