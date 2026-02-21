package com.jdailer.feature.messages.domain.usecase

import com.jdailer.feature.messages.domain.model.RcsCapability
import com.jdailer.feature.messages.domain.repository.MessageCapabilityRepository

class GetRcsCapabilityUseCase(
    private val repository: MessageCapabilityRepository
) {
    suspend operator fun invoke(): RcsCapability = repository.getRcsCapability()
}
