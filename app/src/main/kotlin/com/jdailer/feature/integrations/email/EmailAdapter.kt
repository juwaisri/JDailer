package com.jdailer.feature.integrations.email

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationAppAdapter
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.IntentResult

class EmailAdapter : CommunicationAppAdapter {
    override val id: String = "email"
    override val packageName: String = "android"
    override val supportedActions = setOf(AdapterAction.EMAIL)
    override val priority: Int = 60

    override suspend fun canHandle(context: Context, target: CommunicationTarget): Boolean {
        return !target.emailAddress.isNullOrBlank()
    }

    override suspend fun launch(
        context: Context,
        target: CommunicationTarget,
        action: AdapterAction,
        prefilledText: String?
    ): IntentResult {
        return try {
            val email = target.emailAddress ?: return IntentResult.Unavailable("Email is empty")
            val uri = Uri.parse("mailto:$email")
            val bestPackage = preferMailClient(context)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = uri
                putExtra(Intent.EXTRA_SUBJECT, "Message from JDialer")
                putExtra(Intent.EXTRA_TEXT, prefilledText.orEmpty())
                if (bestPackage != null) setPackage(bestPackage)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Compose email"))
            IntentResult.Success
        } catch (exception: Exception) {
            IntentResult.Failure("Email launch failed", exception)
        }
    }

    private fun preferMailClient(context: Context): String? {
        val preferred = listOf(
            "com.google.android.gm",
            "com.microsoft.office.outlook"
        )
        return preferred.firstOrNull {
            runCatching {
                context.packageManager.getPackageInfo(it, PackageManager.MATCH_ALL)
                true
            }.getOrDefault(false)
        }
    }
}
