package com.jdailer.feature.integrations.base

import android.content.Context

class CommunicationAdapterRegistry(
    private val adapters: List<CommunicationAppAdapter>
) {
    suspend fun findAdapterFor(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction
    ): CommunicationAppAdapter? {
        return adapters
            .filter { it.supportedActions.contains(action) }
            .sortedByDescending { it.priority }
            .firstOrNull { it.canHandle(context, target) }
    }

    suspend fun launchPreferred(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        text: String? = null
    ): IntentResult {
        val adapter = findAdapterFor(context, target, action)
            ?: return IntentResult.Unavailable("No adapter available for action $action")
        return adapter.launch(context, target, action, text)
    }

    suspend fun launchForPlatform(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        platformId: String,
        text: String? = null
    ): IntentResult {
        val adapter = adapters
            .firstOrNull { it.id == platformId }
            ?.takeIf { it.supportedActions.contains(action) && it.canHandle(context, target) }
            ?: return IntentResult.Unavailable("No adapter available for $platformId/$action")
        return adapter.launch(context, target, action, text)
    }
}
