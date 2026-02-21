package com.jdailer.feature.integrations.email

import android.content.Context
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.ExternalPlatform
import com.jdailer.feature.integrations.base.IntentResult
import com.jdailer.feature.integrations.base.PlatformIntentLauncher

class EmailIntegrationService(
    private val platformLauncher: PlatformIntentLauncher
) {
    private val preferredPlatforms = listOf(ExternalPlatform.EMAIL.id)

    suspend fun launchEmail(context: Context, target: CommunicationTarget, body: String?): IntentResult {
        return platformLauncher.launchPreferred(
            context = context,
            target = target,
            action = AdapterAction.EMAIL,
            preferred = preferredPlatforms,
            prefilledText = body
        )
    }
}
