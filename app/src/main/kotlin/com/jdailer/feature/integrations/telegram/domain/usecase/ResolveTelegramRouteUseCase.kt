package com.jdailer.feature.integrations.telegram.domain.usecase

import android.content.Context
import android.content.pm.PackageManager
import com.jdailer.feature.integrations.telegram.domain.model.TelegramRouteProfile

class ResolveTelegramRouteUseCase(
    private val context: Context
) {
    private val packageCandidates = listOf("org.telegram.messenger", "org.thunderdog.challegram")

    operator fun invoke(rawNumber: String): TelegramRouteProfile {
        if (rawNumber.isBlank()) {
            return TelegramRouteProfile(
                canLaunch = false,
                selectedPackage = null,
                supportsVoice = false,
                isInstalled = false,
                reason = "No phone number for Telegram"
            )
        }

        val selectedPackage = packageCandidates.firstOrNull { isPackageInstalled(it) }
        if (selectedPackage == null) {
            return TelegramRouteProfile(
                canLaunch = false,
                selectedPackage = null,
                supportsVoice = false,
                isInstalled = false,
                reason = "Telegram package is not installed"
            )
        }

        return TelegramRouteProfile(
            canLaunch = true,
            selectedPackage = selectedPackage,
            supportsVoice = true,
            isInstalled = true,
            reason = "Resolved by $selectedPackage"
        )
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return runCatching {
            context.packageManager.getPackageInfo(packageName, PackageManager.MATCH_ALL)
            true
        }.getOrDefault(false)
    }
}
