package com.jdailer.feature.integrations.whatsapp

import android.content.Context
import android.content.pm.PackageManager
import com.jdailer.feature.integrations.whatsapp.model.WhatsAppRouteProfile

class ResolveWhatsAppRouteUseCase(
    private val context: Context
) {
    private val packages = listOf("com.whatsapp", "com.whatsapp.w4b")

    operator fun invoke(rawNumber: String): WhatsAppRouteProfile {
        if (rawNumber.isBlank()) {
            return WhatsAppRouteProfile(
                canLaunch = false,
                useBusinessEndpoint = false,
                selectedPackage = null,
                canPlaceCall = false,
                reason = "No phone number"
            )
        }

        val normalized = rawNumber.filter { it.isDigit() || it == '+' }
        val resolvedPackage = packages.firstOrNull { isPackageInstalled(it) }

        if (resolvedPackage == null) {
            return WhatsAppRouteProfile(
                canLaunch = false,
                useBusinessEndpoint = false,
                selectedPackage = null,
                canPlaceCall = false,
                reason = "WhatsApp app missing"
            )
        }

        return WhatsAppRouteProfile(
            canLaunch = true,
            useBusinessEndpoint = resolvedPackage == "com.whatsapp.w4b",
            selectedPackage = resolvedPackage,
            canPlaceCall = true,
            reason = "Resolved by $resolvedPackage"
        )
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return runCatching {
            context.packageManager.getPackageInfo(packageName, PackageManager.MATCH_ALL)
            true
        }.getOrDefault(false)
    }
}
