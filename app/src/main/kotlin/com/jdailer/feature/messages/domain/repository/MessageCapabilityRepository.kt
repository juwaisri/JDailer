package com.jdailer.feature.messages.domain.repository

import com.jdailer.feature.messages.domain.model.MessageAttachmentMeta
import com.jdailer.feature.messages.domain.model.MessageAttachmentPolicy
import com.jdailer.feature.messages.domain.model.RcsCapability

interface MessageCapabilityRepository {
    suspend fun getRcsCapability(): RcsCapability
    suspend fun inspectAttachments(
        attachmentUris: List<String>,
        policy: MessageAttachmentPolicy = MessageAttachmentPolicy()
    ): List<MessageAttachmentMeta>
}
