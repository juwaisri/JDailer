package com.jdailer.feature.voip.data.repository

import com.jdailer.feature.voip.domain.model.SipProfile
import com.jdailer.feature.voip.domain.repository.SipProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemorySipProfileRepository(
    initialProfile: SipProfile = SipProfile(
        profileId = "default",
        domain = "",
        username = "",
        password = "",
        transport = "UDP",
        port = 5060,
        outboundProxy = null,
        enabled = false
    )
) : SipProfileRepository {
    private val profileFlow = MutableStateFlow(initialProfile)

    override fun observeProfile(): Flow<SipProfile> = profileFlow

    override suspend fun save(profile: SipProfile) {
        profileFlow.value = profile
    }
}
