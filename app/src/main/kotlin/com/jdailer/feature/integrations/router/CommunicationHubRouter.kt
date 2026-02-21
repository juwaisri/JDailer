package com.jdailer.feature.integrations.router

import android.content.Context
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationAdapterRegistry
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.ExternalPlatform
import com.jdailer.feature.integrations.base.IntentResult
import com.jdailer.feature.integrations.linking.domain.repository.ExternalConversationLinkRepository

class CommunicationHubRouter(
    private val registry: CommunicationAdapterRegistry,
    private val linkRepository: ExternalConversationLinkRepository
) {
    suspend fun launchMessage(
        context: Context,
        target: CommunicationTarget,
        text: String? = null
    ): IntentResult {
        val preferred = resolvePreferredPlatform(target.contactId, preferred = ExternalPlatform.WHATSAPP.id)
            ?: resolvePreferredPlatform(target.contactId, preferred = ExternalPlatform.TELEGRAM.id)
            ?: resolvePreferredPlatform(target.contactId, preferred = ExternalPlatform.SIGNAL.id)

        return preferred?.let { platform ->
            registry.launchForPlatform(context, target, AdapterAction.MESSAGE, platform.id, text)
        } ?: run {
            registry.launchPreferred(context, target, AdapterAction.MESSAGE, text)
        }
    }

    suspend fun launchCall(
        context: Context,
        target: CommunicationTarget
    ): IntentResult = registry.launchPreferred(context, target, AdapterAction.CALL)

    suspend fun launchEmail(
        context: Context,
        target: CommunicationTarget,
        text: String? = null
    ): IntentResult = registry.launchPreferred(context, target, AdapterAction.EMAIL, text)

    private suspend fun resolvePreferredPlatform(
        contactId: Long?,
        preferred: String
    ): ExternalPlatform? {
        if (contactId == null) return null
        return linkRepository.getLink(contactId, preferred)?.let { link ->
            val platformId = link.platform
            ExternalPlatform.values().firstOrNull { platform ->
                platform.id == platformId && link.isEnabled && !link.isBlocked
            }
        }
    }
}
