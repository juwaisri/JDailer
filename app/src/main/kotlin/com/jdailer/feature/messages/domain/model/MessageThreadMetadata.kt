package com.jdailer.feature.messages.domain.model

data class MessageThreadMetadata(
    val isRcs: Boolean = false,
    val isGroupConversation: Boolean = false,
    val participantCount: Int = 0,
    val hasMedia: Boolean = false,
    val attachmentCount: Int = 0,
    val attachmentType: String? = null,
    val secureChannel: Boolean = false
) {
    val hasParticipants: Boolean
        get() = participantCount > 0

    val isGroup: Boolean
        get() = isGroupConversation || participantCount > 2

    companion object {
        val Empty = MessageThreadMetadata()
    }
}

