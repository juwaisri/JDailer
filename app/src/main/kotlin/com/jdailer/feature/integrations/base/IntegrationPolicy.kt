package com.jdailer.feature.integrations.base

import android.content.Context
import com.jdailer.core.privacy.IntegrationPrivacyPolicyStore
import kotlinx.coroutines.flow.first

sealed interface IntegrationPolicyDecision {
    data object Allowed : IntegrationPolicyDecision
    data class Blocked(val reason: String) : IntegrationPolicyDecision
}

interface IntegrationPolicyService {
    suspend fun evaluate(
        _: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        platformId: String? = null
    ): IntegrationPolicyDecision
}

object NoopIntegrationPolicyService : IntegrationPolicyService {
    override suspend fun evaluate(
        _: Context,
        _: CommunicationTarget,
        _: AdapterAction,
        _: String?
    ): IntegrationPolicyDecision {
        return IntegrationPolicyDecision.Allowed
    }
}

class DefaultIntegrationPolicyService(
    private val policyStore: IntegrationPrivacyPolicyStore
) : IntegrationPolicyService {
    override suspend fun evaluate(
        _: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        platformId: String?
    ): IntegrationPolicyDecision {
        val policy = policyStore.policy.first()
        if (!policy.allowThirdPartyIntegrations) {
            return IntegrationPolicyDecision.Blocked("Third-party integrations are disabled by policy.")
        }

        if (target.mediaUris.isNotEmpty() && !policy.allowMessageMedia) {
            return IntegrationPolicyDecision.Blocked("Media attachment sharing is disabled by policy.")
        }

        if (!policy.allowThirdPartyMessaging && action == AdapterAction.MESSAGE) {
            return IntegrationPolicyDecision.Blocked("Message actions are blocked by messaging policy.")
        }

        if (!policy.allowThirdPartyCalls && action == AdapterAction.CALL) {
            return IntegrationPolicyDecision.Blocked("Call actions are blocked by policy.")
        }

        if (action == AdapterAction.EMAIL && !policy.allowEmail) {
            return IntegrationPolicyDecision.Blocked("Email actions are blocked by policy.")
        }

        return when (platformId?.lowercase()) {
            ExternalPlatform.WHATSAPP.id -> if (policy.allowWhatsApp) {
                IntegrationPolicyDecision.Allowed
            } else {
                IntegrationPolicyDecision.Blocked("WhatsApp is disabled by policy.")
            }
            ExternalPlatform.TELEGRAM.id -> if (policy.allowTelegram) {
                IntegrationPolicyDecision.Allowed
            } else {
                IntegrationPolicyDecision.Blocked("Telegram is disabled by policy.")
            }
            ExternalPlatform.SIGNAL.id -> if (policy.allowSignal) {
                IntegrationPolicyDecision.Allowed
            } else {
                IntegrationPolicyDecision.Blocked("Signal is disabled by policy.")
            }
            ExternalPlatform.EMAIL.id -> if (policy.allowEmail) {
                IntegrationPolicyDecision.Allowed
            } else {
                IntegrationPolicyDecision.Blocked("Email is disabled by policy.")
            }
            null -> IntegrationPolicyDecision.Allowed
            else -> IntegrationPolicyDecision.Allowed
        }
    }

}
