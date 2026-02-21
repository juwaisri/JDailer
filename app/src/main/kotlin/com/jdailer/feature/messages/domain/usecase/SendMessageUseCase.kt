package com.jdailer.feature.messages.domain.usecase

import com.jdailer.feature.messages.domain.repository.UnifiedMessageRepository

class SendMessageUseCase(
    private val repository: UnifiedMessageRepository
) {
    suspend fun sendText(phone: String, text: String, threadId: String? = null): Result<String> {
        return repository.sendSms(phone, text, threadId)
    }

    suspend fun sendMms(
        phone: String,
        text: String?,
        attachmentUris: List<String>
    ): Result<String> {
        return repository.sendMms(phone, text, attachmentUris)
    }
}
