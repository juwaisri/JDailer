package com.jdailer.feature.integrations.whatsapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationAppAdapter
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.IntentResult

class WhatsAppAdapter : CommunicationAppAdapter {
    override val id: String = "whatsapp"
    override val packageName: String = "com.whatsapp"
    override val supportedActions = setOf(AdapterAction.MESSAGE, AdapterAction.CALL)
    override val priority: Int = 100
    private val packageCheckCache = mutableMapOf<String, Boolean>()

    override suspend fun canHandle(context: Context, target: CommunicationTarget): Boolean {
        if (target.phoneNumber.isNullOrBlank()) return false
        return isAnyPackageAvailable(context, candidatePackages())
    }

    override suspend fun launch(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        prefilledText: String?
    ): IntentResult {
        return runCatching {
            if (target.phoneNumber.orEmpty().isBlank()) {
                return IntentResult.Unavailable("Phone number is empty")
            }

            val normalized = target.phoneNumber.orEmpty().filter { it.isDigit() || it == '+' }
            val packageToUse = resolveAvailablePackage(context, candidatePackages())
            val uri = when (action) {
                AdapterAction.CALL -> "https://wa.me/$normalized"
                else -> "https://wa.me/$normalized?text=${Uri.encode(prefilledText.orEmpty())}"
            }

            val baseIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(uri)
                putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + context.packageName))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (!packageToUse.isNullOrBlank()) {
                baseIntent.setPackage(packageToUse)
            }
            context.startActivity(baseIntent)
            IntentResult.Success
        }.getOrElse { exception ->
            IntentResult.Failure("WhatsApp launch failed", exception)
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return packageCheckCache.getOrPut(packageName) {
            runCatching {
                context.packageManager.getPackageInfo(packageName, PackageManager.MATCH_ALL)
                true
            }.getOrDefault(false)
        }
    }

    private fun candidatePackages(): List<String> {
        return listOf(
            packageName,
            "com.whatsapp.w4b"
        )
    }

    private fun resolveAvailablePackage(context: Context, candidates: List<String>): String? {
        return candidates.firstOrNull { isPackageInstalled(context, it) }
    }

    private fun isAnyPackageAvailable(context: Context, candidates: List<String>): Boolean {
        return candidates.any { isPackageInstalled(context, it) }
    }
}
