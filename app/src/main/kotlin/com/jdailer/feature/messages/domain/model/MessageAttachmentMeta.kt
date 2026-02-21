package com.jdailer.feature.messages.domain.model

data class MessageAttachmentMeta(
    val uri: String,
    val fileName: String?,
    val mimeType: String?,
    val bytes: Long,
    val width: Int? = null,
    val height: Int? = null
)
