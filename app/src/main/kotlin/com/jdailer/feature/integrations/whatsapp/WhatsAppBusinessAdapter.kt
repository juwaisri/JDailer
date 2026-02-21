package com.jdailer.feature.integrations.whatsapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationAppAdapter
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.IntentResult

class WhatsAppBusinessAdapter : CommunicationAppAdapter {
    override val id: String = "whatsapp_business"
    override val packageName: String = "com.whatsapp.w4b"
    override val supportedActions = setOf(AdapterAction.MESSAGE)
    override val priority: Int = 90

    override suspend fun canHandle(context: Context, target: CommunicationTarget): Boolean {
        if (target.phoneNumber.orEmpty().isBlank()) return false
        return isPackageInstalled(context, packageName)
    }

    override suspend fun launch(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        prefilledText: String?
    ): IntentResult {
        return try {
            if (target.phoneNumber.orEmpty().isBlank()) {
                return IntentResult.Unavailable("Phone number is empty")
            }
            val number = target.phoneNumber.orEmpty().filter { it.isDigit() || it == '+' }
            val fallbackAction = AdapterAction.MESSAGE
            val encodedText = Uri.encode(prefilledText.orEmpty())
            val primary = Uri.parse("https://wa.me/$number?text=$encodedText")
            val fallback = Uri.parse("https://wa.me/$number?text=$encodedText")

            val targetUri = when (fallbackAction) {
                AdapterAction.MESSAGE -> primary
                else -> primary
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = targetUri
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (isPackageInstalled(context, packageName)) {
                context.startActivity(intent)
                IntentResult.Success
            } else {
                context.startActivity(Intent(Intent.ACTION_VIEW, fallback).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                IntentResult.Success
            }
        } catch (exception: Exception) {
            IntentResult.Failure("WhatsApp Business launch failed", exception)
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return runCatching {
            context.packageManager.getPackageInfo(packageName, PackageManager.MATCH_ALL)
            true
        }.getOrDefault(false)
    }
}
