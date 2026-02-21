package com.jdailer.feature.integrations.email.domain.usecase

import android.content.Context
import android.content.pm.PackageManager
import com.jdailer.feature.integrations.email.domain.model.EmailRouteProfile

class ResolveEmailRouteUseCase(
    private val context: Context
) {
    private val preferredMailClients = listOf(
        "com.google.android.gm",
        "com.microsoft.office.outlook",
        "com.yahoo.mobile.client.android.mail"
    )

    operator fun invoke(email: String?): EmailRouteProfile {
        if (email.isNullOrBlank()) {
            return EmailRouteProfile(
                canCompose = false,
                selectedPackage = null,
                includeBcc = false,
                reason = "Email address not found"
            )
        }

        val resolvedClient = preferredMailClients.firstOrNull { packageName ->
            runCatching {
                context.packageManager.getPackageInfo(packageName, PackageManager.MATCH_ALL)
                true
            }.getOrDefault(false)
        }

        return EmailRouteProfile(
            canCompose = true,
            selectedPackage = resolvedClient,
            includeBcc = false,
            reason = resolvedClient?.let { "Composer with $it" } ?: "No preferred client; Android chooser"
        )
    }
}
