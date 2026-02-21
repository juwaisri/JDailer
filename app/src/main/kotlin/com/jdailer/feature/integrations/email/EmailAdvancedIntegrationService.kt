package com.jdailer.feature.integrations.email

import android.content.Context
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.ExternalPlatform
import com.jdailer.feature.integrations.base.IntentResult
import com.jdailer.feature.integrations.base.PlatformIntentLauncher
import com.jdailer.feature.integrations.email.domain.usecase.ResolveEmailRouteUseCase

class EmailAdvancedIntegrationService(
    private val routeResolver: ResolveEmailRouteUseCase,
    private val platformLauncher: PlatformIntentLauncher
) {
    suspend fun launchEmail(
        context: Context,
        target: CommunicationTarget,
        body: String?
    ): IntentResult {
        val profile = routeResolver(target.emailAddress)
        if (!profile.canCompose) {
            return IntentResult.Unavailable(profile.reason)
        }

        return platformLauncher.launchForPlatform(
            context = context,
            target = target,
            action = AdapterAction.EMAIL,
            platformId = ExternalPlatform.EMAIL.id,
            prefilledText = body
        )
    }
}
