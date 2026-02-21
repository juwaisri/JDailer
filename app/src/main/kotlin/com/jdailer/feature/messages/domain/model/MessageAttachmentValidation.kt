package com.jdailer.feature.messages.domain.model

sealed interface MessageAttachmentValidation {
    data class Accepted(val attachments: List<MessageAttachmentMeta>) : MessageAttachmentValidation

    data class Rejected(
        val reason: String,
        val rejectedUris: List<String>,
        val blockedBytes: Long? = null
    ) : MessageAttachmentValidation
}
