package com.jdailer.feature.messages.domain.usecase

import com.jdailer.feature.messages.domain.model.MessageAttachmentValidation

class SendMessageWithPolicyUseCase(
    private val sendMessageUseCase: SendMessageUseCase,
    private val attachmentValidation: ValidateMessageAttachmentPolicyUseCase
) {
    suspend operator fun invoke(
        phone: String,
        text: String?,
        attachmentUris: List<String>,
        threadId: String? = null
    ): Result<String> = sendSmsOrMms(phone, text, attachmentUris, threadId)

    suspend fun sendSmsOrMms(
        phone: String,
        text: String?,
        attachmentUris: List<String>,
        threadId: String? = null
    ): Result<String> {
        val trimmedText = text.orEmpty().trim()
        if (trimmedText.isBlank() && attachmentUris.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cannot send empty message"))
        }

        if (attachmentUris.isNotEmpty()) {
            return when (val validation = attachmentValidation(attachmentUris)) {
                is MessageAttachmentValidation.Rejected -> Result.failure(IllegalArgumentException(validation.reason))
                is MessageAttachmentValidation.Accepted -> sendMessageUseCase.sendMms(phone, trimmedText.ifBlank { null }, attachmentUris)
            }
        }

        return sendMessageUseCase.sendText(phone, trimmedText, threadId)
    }
}
