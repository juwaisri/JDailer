package com.jdailer.feature.voip.domain.usecase

import com.jdailer.feature.voip.domain.model.VoipSession
import com.jdailer.feature.voip.domain.repository.VoipRepository
import kotlinx.coroutines.flow.Flow

class ObserveVoipSessionsUseCase(
    private val repository: VoipRepository
) {
    operator fun invoke(): Flow<List<VoipSession>> = repository.observeSessions()
}
