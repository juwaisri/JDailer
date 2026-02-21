package com.jdailer.feature.messages.domain.usecase

import com.jdailer.feature.messages.domain.model.MessageAttachmentPolicy
import com.jdailer.feature.messages.domain.model.MessageAttachmentValidation
import com.jdailer.feature.messages.domain.repository.MessageCapabilityRepository

class ValidateMessageAttachmentPolicyUseCase(
    private val repository: MessageCapabilityRepository,
    private val policy: MessageAttachmentPolicy = MessageAttachmentPolicy()
) {
    suspend operator fun invoke(attachmentUris: List<String>): MessageAttachmentValidation {
        if (attachmentUris.isEmpty()) return MessageAttachmentValidation.Accepted(emptyList())

        if (attachmentUris.size > policy.maxAttachments) {
            return MessageAttachmentValidation.Rejected(
                "Attachment limit exceeded",
                attachmentUris
            )
        }

        val metadata = repository.inspectAttachments(attachmentUris, policy)
        if (metadata.size != attachmentUris.size) {
            return MessageAttachmentValidation.Rejected(
                "One or more media items cannot be inspected",
                attachmentUris.filterNot { raw -> metadata.any { it.uri == raw } }
            )
        }

        val disallowedSchemes = setOf("content://com.google.android.apps.nbu.paisaapi", "file:///proc")
        var totalBytes = 0L

        for (item in metadata) {
            if (item.bytes > policy.maxSingleAttachmentBytes) {
                return MessageAttachmentValidation.Rejected(
                    "Attachment too large: ${item.uri}",
                    listOf(item.uri),
                    blockedBytes = item.bytes
                )
            }

            val mime = item.mimeType?.lowercase()?.trim() ?: ""
            if (mime.isBlank()) {
                return MessageAttachmentValidation.Rejected(
                    "Missing MIME type for attachment",
                    listOf(item.uri)
                )
            }

            if (policy.disallowDocumentByDefault && mime !in allowedPrefixes(policy)) {
                if (!mime.startsWith("image/") && !mime.startsWith("video/") && !mime.startsWith("audio/") && !mime.startsWith("text/")) {
                    return MessageAttachmentValidation.Rejected(
                        "Unsupported attachment type: $mime",
                        listOf(item.uri)
                    )
                }
            }

            if (policy.allowedMimePrefixes.isNotEmpty()) {
                val prefixAllowed = policy.allowedMimePrefixes.any { prefix ->
                    mime.startsWith(prefix)
                }
                if (!prefixAllowed) {
                    return MessageAttachmentValidation.Rejected(
                        "Attachment type not permitted by policy",
                        listOf(item.uri)
                    )
                }
            }

            if (disallowedSchemes.any { item.uri.startsWith(it) }) {
                return MessageAttachmentValidation.Rejected(
                    "Attachment source is not permitted",
                    listOf(item.uri)
                )
            }

            if (mime.startsWith("image/") && policy.maxImageWidth > 0 && policy.maxImageHeight > 0) {
                val width = item.width
                val height = item.height
                if ((width != null && width > policy.maxImageWidth) || (height != null && height > policy.maxImageHeight)) {
                    return MessageAttachmentValidation.Rejected(
                        "Image exceeds allowed dimensions",
                        listOf(item.uri)
                    )
                }
            }

            totalBytes += item.bytes
            if (totalBytes > policy.maxTotalBytes) {
                return MessageAttachmentValidation.Rejected(
                    "Combined attachments exceed policy quota",
                    attachmentUris,
                    blockedBytes = totalBytes
                )
            }
        }

        return MessageAttachmentValidation.Accepted(metadata)
    }

    private fun allowedPrefixes(policy: MessageAttachmentPolicy): Set<String> =
        policy.allowedMimePrefixes.takeIf { it.isNotEmpty() } ?: emptySet()
}
