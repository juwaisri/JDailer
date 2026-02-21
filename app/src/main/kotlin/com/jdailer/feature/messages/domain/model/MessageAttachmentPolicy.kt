package com.jdailer.feature.messages.domain.model

data class MessageAttachmentPolicy(
    val maxAttachments: Int = 8,
    val maxSingleAttachmentBytes: Long = 10L * 1024L * 1024L,
    val maxTotalBytes: Long = 24L * 1024L * 1024L,
    val maxImageWidth: Int = 2048,
    val maxImageHeight: Int = 2048,
    val allowedMimePrefixes: Set<String> = setOf(
        "image/",
        "video/",
        "audio/"
    ),
    val disallowDocumentByDefault: Boolean = true
)
