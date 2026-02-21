package com.jdailer.feature.integrations.signal

import android.content.Context
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.ExternalPlatform
import com.jdailer.feature.integrations.base.IntentResult
import com.jdailer.feature.integrations.base.PlatformIntentLauncher

class SignalIntegrationService(
    private val platformLauncher: PlatformIntentLauncher
) {
    private val preferredPlatforms = listOf(ExternalPlatform.SIGNAL.id)

    suspend fun launchMessage(context: Context, target: CommunicationTarget, text: String?): IntentResult {
        return platformLauncher.launchPreferred(
            context = context,
            target = target,
            action = AdapterAction.MESSAGE,
            preferred = preferredPlatforms,
            prefilledText = text
        )
    }
}
