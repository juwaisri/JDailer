package com.jdailer.feature.integrations.base

import android.content.Context

class PlatformIntentLauncher(
    private val registry: CommunicationAdapterRegistry,
    private val policyService: IntegrationPolicyService = NoopIntegrationPolicyService
) {
    suspend fun launchForPlatform(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        platformId: String,
        prefilledText: String? = null
    ): IntentResult {
        return when (val policy = policyService.evaluate(context, target, action, platformId)) {
            is IntegrationPolicyDecision.Allowed -> registry.launchForPlatform(
                context,
                target,
                action,
                platformId,
                prefilledText
            )
            is IntegrationPolicyDecision.Blocked -> IntentResult.Unavailable(policy.reason)
        }
    }

    suspend fun launchPreferred(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        preferred: List<String>,
        prefilledText: String? = null
    ): IntentResult {
        preferred.forEach { platformId ->
            when (val policy = policyService.evaluate(context, target, action, platformId)) {
                is IntegrationPolicyDecision.Allowed -> {
                    when (val platformResult = registry.launchForPlatform(
                        context,
                        target,
                        action,
                        platformId,
                        prefilledText
                    )) {
                        is IntentResult.Success -> return platformResult
                        else -> Unit
                    }
                }
                is IntegrationPolicyDecision.Blocked -> Unit
            }
        }

        return when (val fallbackPolicy = policyService.evaluate(context, target, action, null)) {
            is IntegrationPolicyDecision.Allowed -> registry.launchPreferred(context, target, action, prefilledText)
            is IntegrationPolicyDecision.Blocked -> IntentResult.Unavailable(fallbackPolicy.reason)
        }
    }
}

