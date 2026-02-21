package com.jdailer.feature.call.spam.domain.usecase

import com.jdailer.feature.call.spam.CallerIdService

class BlockCallerUseCase(
    private val callerIdService: CallerIdService
) {
    suspend operator fun invoke(number: String, blocked: Boolean) {
        callerIdService.block(number, blocked)
    }
}
