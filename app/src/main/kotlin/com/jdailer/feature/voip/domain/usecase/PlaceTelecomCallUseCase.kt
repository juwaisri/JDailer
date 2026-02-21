package com.jdailer.feature.voip.domain.usecase

import com.jdailer.feature.voip.data.telecom.JDialerTelecomManager
import com.jdailer.feature.voip.domain.model.CallRouteResult

class PlaceTelecomCallUseCase(
    private val telecomManager: JDialerTelecomManager
) {
    operator fun invoke(
        address: String,
        useSipUri: Boolean = false
    ): CallRouteResult {
        return telecomManager.launchOutgoingCall(address, useSipUri)
    }
}
