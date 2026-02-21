package com.jdailer.feature.integrations.signal

import android.content.Context
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.ExternalPlatform
import com.jdailer.feature.integrations.base.IntentResult
import com.jdailer.feature.integrations.base.PlatformIntentLauncher
import com.jdailer.feature.integrations.signal.domain.usecase.ResolveSignalRouteUseCase

class SignalAdvancedIntegrationService(
    private val routeResolver: ResolveSignalRouteUseCase,
    private val platformLauncher: PlatformIntentLauncher
) {
    suspend fun launchMessage(
        context: Context,
        target: CommunicationTarget,
        text: String?
    ): IntentResult {
        val profile = routeResolver(target.phoneNumber.orEmpty())
        if (!profile.canLaunch) {
            return IntentResult.Unavailable(profile.reason)
        }

        return platformLauncher.launchForPlatform(
            context = context,
            target = target,
            action = AdapterAction.MESSAGE,
            platformId = ExternalPlatform.SIGNAL.id,
            prefilledText = text
        )
    }
}
