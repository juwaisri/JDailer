package com.jdailer.feature.voip.domain.usecase

import com.jdailer.feature.voip.domain.model.SipProfile
import com.jdailer.feature.voip.domain.repository.SipProfileRepository

class SaveSipProfileUseCase(
    private val repository: SipProfileRepository
) {
    suspend operator fun invoke(profile: SipProfile) {
        repository.save(profile)
    }
}
