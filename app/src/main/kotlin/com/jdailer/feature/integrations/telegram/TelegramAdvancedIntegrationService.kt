package com.jdailer.feature.integrations.telegram

import android.content.Context
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.ExternalPlatform
import com.jdailer.feature.integrations.base.IntentResult
import com.jdailer.feature.integrations.base.PlatformIntentLauncher
import com.jdailer.feature.integrations.telegram.domain.usecase.ResolveTelegramRouteUseCase

class TelegramAdvancedIntegrationService(
    private val routeResolver: ResolveTelegramRouteUseCase,
    private val platformLauncher: PlatformIntentLauncher
) {
    suspend fun launchMessage(
        context: Context,
        target: CommunicationTarget,
        text: String?
    ): IntentResult {
        return launch(context, target, AdapterAction.MESSAGE, text, asVoice = false)
    }

    suspend fun launchCall(
        context: Context,
        target: CommunicationTarget
    ): IntentResult {
        return launch(context, target, AdapterAction.CALL, prefilledText = null, asVoice = true)
    }

    private suspend fun launch(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        prefilledText: String?,
        asVoice: Boolean
    ): IntentResult {
        val profile = routeResolver(target.phoneNumber.orEmpty())
        if (!profile.canLaunch) {
            return IntentResult.Unavailable(profile.reason)
        }
        if (asVoice && !profile.supportsVoice) {
            return IntentResult.Unavailable("Telegram voice call is unavailable on this installation")
        }

        return platformLauncher.launchForPlatform(
            context = context,
            target = target,
            action = action,
            platformId = ExternalPlatform.TELEGRAM.id,
            prefilledText = prefilledText
        )
    }
}
