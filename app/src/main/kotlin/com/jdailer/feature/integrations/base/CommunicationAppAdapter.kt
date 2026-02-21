package com.jdailer.feature.integrations.base

import android.content.Context

interface CommunicationAppAdapter {
    val id: String
    val packageName: String
    val supportedActions: Set<AdapterAction>
    val priority: Int

    suspend fun canHandle(context: Context, target: CommunicationTarget): Boolean
    suspend fun launch(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        prefilledText: String? = null
    ): IntentResult
}
