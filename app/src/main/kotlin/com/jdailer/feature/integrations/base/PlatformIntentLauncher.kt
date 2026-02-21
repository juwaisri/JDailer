package com.jdailer.feature.integrations.base

import android.content.Context

class PlatformIntentLauncher(
    private val registry: CommunicationAdapterRegistry
) {
    suspend fun launchForPlatform(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        platformId: String,
        prefilledText: String? = null
    ): IntentResult {
        return registry.launchForPlatform(context, target, action, platformId, prefilledText)
    }

    suspend fun launchPreferred(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        preferred: List<String>,
        prefilledText: String? = null
    ): IntentResult {
        preferred.forEach { platformId ->
            when (val platformResult = launchForPlatform(context, target, action, platformId, prefilledText)) {
                is IntentResult.Success -> return platformResult
                else -> Unit
            }
        }

        return registry.launchPreferred(context, target, action, prefilledText)
    }
}
