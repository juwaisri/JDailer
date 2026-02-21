package com.jdailer.feature.integrations.telegram

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationAppAdapter
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.IntentResult

class TelegramAdapter : CommunicationAppAdapter {
    override val id: String = "telegram"
    override val packageName: String = "org.telegram.messenger"
    override val supportedActions = setOf(AdapterAction.MESSAGE, AdapterAction.CALL)
    override val priority: Int = 80
    private val fallbackPackages = listOf("org.thunderdog.challegram", "org.telegram.messenger")

    override suspend fun canHandle(context: Context, target: CommunicationTarget): Boolean {
        if (target.phoneNumber.isNullOrBlank()) return false
        return fallbackPackages.any { isPackageInstalled(context, it) }
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
            val preferredPackage = fallbackPackages.firstOrNull { isPackageInstalled(context, it) }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = if (action == AdapterAction.MESSAGE) {
                    Uri.parse("tg://msg?to=$number&text=${Uri.encode(prefilledText.orEmpty())}")
                } else {
                    Uri.parse("tg://call?to=$number")
                }
                preferredPackage?.let(::setPackage)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (preferredPackage == null) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$number")))
            } else {
                context.startActivity(intent)
            }
            IntentResult.Success
        } catch (exception: Exception) {
            IntentResult.Failure("Telegram launch failed", exception)
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return runCatching {
            context.packageManager.getPackageInfo(packageName, PackageManager.MATCH_ALL)
            true
        }.getOrDefault(false)
    }
}
