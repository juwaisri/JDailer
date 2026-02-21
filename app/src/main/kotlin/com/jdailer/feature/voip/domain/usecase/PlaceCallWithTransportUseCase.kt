package com.jdailer.feature.voip.domain.usecase

import com.jdailer.feature.voip.data.telecom.JDialerTelecomManager
import com.jdailer.feature.voip.domain.model.CallTransportDecision
import com.jdailer.feature.voip.domain.model.CallTransportType

class PlaceCallWithTransportUseCase(
    private val transportResolver: ResolveCallTransportUseCase,
    private val telecomManager: JDialerTelecomManager
) {
    suspend operator fun invoke(
        rawAddress: String,
        preferSip: Boolean = false
    ): CallTransportDecision {
        val decision = transportResolver(rawAddress, preferSip)

        return when (decision.transportType) {
            CallTransportType.TELECOM, CallTransportType.SIP -> {
                telecomManager.launchOutgoingCall(decision.address, useSipUri = decision.useSipUri)
                decision.copy(routed = true)
            }
            CallTransportType.FALLBACK_DIAL -> {
                telecomManager.launchOutgoingCall(decision.address, useSipUri = false)
                decision.copy(routed = true)
            }
            CallTransportType.BLOCKED -> decision
        }
    }
}
