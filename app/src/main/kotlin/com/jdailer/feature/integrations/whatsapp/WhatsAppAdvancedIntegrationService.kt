package com.jdailer.feature.integrations.whatsapp

import android.content.Context
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.ExternalPlatform
import com.jdailer.feature.integrations.base.IntentResult
import com.jdailer.feature.integrations.base.PlatformIntentLauncher
import com.jdailer.feature.integrations.whatsapp.model.WhatsAppRouteProfile

class WhatsAppAdvancedIntegrationService(
    private val routeResolver: ResolveWhatsAppRouteUseCase,
    private val platformLauncher: PlatformIntentLauncher
) {
    private val fallbackText = "WhatsApp"

    suspend fun launchMessage(
        context: Context,
        target: CommunicationTarget,
        prefilledText: String? = null
    ): IntentResult {
        return launch(context, target, AdapterAction.MESSAGE, prefilledText, requiresCall = false)
    }

    suspend fun launchCall(
        context: Context,
        target: CommunicationTarget
    ): IntentResult {
        return launch(context, target, AdapterAction.CALL, prefilledText = null, requiresCall = true)
    }

    private suspend fun launch(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        prefilledText: String?,
        requiresCall: Boolean
    ): IntentResult {
        val profile = routeResolver(target.phoneNumber.orEmpty())

        return if (!profile.canLaunch) {
            IntentResult.Unavailable(profile.reason.ifBlank { "WhatsApp not available" })
        } else if (requiresCall && !profile.canPlaceCall) {
            IntentResult.Unavailable("WhatsApp calling is not supported on this build")
        } else {
            val result = platformLauncher.launchForPlatform(
                context = context,
                target = target,
                action = action,
                platformId = ExternalPlatform.WHATSAPP.id,
                prefilledText = prefilledText
            )

            if (result is IntentResult.Success) {
                result
            } else {
                IntentResult.Failure(
                    "Failed to open WhatsApp (${profile.selectedPackage ?: "unknown"})",
                    null
                )
            }
        }
    }
}
