package com.jdailer.feature.integrations.signal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationAppAdapter
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.IntentResult

class SignalAdapter : CommunicationAppAdapter {
    override val id: String = "signal"
    override val packageName: String = "org.thoughtcrime.securesms"
    override val supportedActions = setOf(AdapterAction.MESSAGE)
    override val priority: Int = 70

    override suspend fun canHandle(context: Context, target: CommunicationTarget): Boolean {
        if (target.phoneNumber.isNullOrBlank()) return false
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
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("smsto:${target.phoneNumber}")
                putExtra("sms_body", prefilledText)
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (isPackageInstalled(context, packageName)) {
                context.startActivity(intent)
                IntentResult.Success
            } else {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://signal.me/#p/${target.phoneNumber}")).apply {
                        putExtra("body", prefilledText)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
                IntentResult.Success
            }
        } catch (exception: Exception) {
            IntentResult.Failure("Signal launch failed", exception)
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return runCatching {
            context.packageManager.getPackageInfo(packageName, PackageManager.MATCH_ALL)
            true
        }.getOrDefault(false)
    }
}
