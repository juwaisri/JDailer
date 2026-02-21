package com.jdailer.feature.voip.domain.repository

import com.jdailer.feature.voip.domain.model.SipProfile
import kotlinx.coroutines.flow.Flow

interface SipProfileRepository {
    fun observeProfile(): Flow<SipProfile>
    suspend fun save(profile: SipProfile)
}
