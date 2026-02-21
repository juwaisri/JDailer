package com.jdailer.feature.voip.domain.usecase

import com.jdailer.feature.voip.domain.model.SipProfile
import com.jdailer.feature.voip.domain.repository.SipProfileRepository
import kotlinx.coroutines.flow.Flow

class GetSipProfileUseCase(
    private val repository: SipProfileRepository
) {
    operator fun invoke(): Flow<SipProfile> = repository.observeProfile()
}
