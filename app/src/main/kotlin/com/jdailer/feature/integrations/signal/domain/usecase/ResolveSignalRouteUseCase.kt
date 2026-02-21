package com.jdailer.feature.integrations.signal.domain.usecase

import android.content.Context
import android.content.pm.PackageManager
import com.jdailer.feature.integrations.signal.domain.model.SignalRouteProfile

class ResolveSignalRouteUseCase(
    private val context: Context
) {
    private val packageName = "org.thoughtcrime.securesms"

    operator fun invoke(rawNumber: String): SignalRouteProfile {
        if (rawNumber.isBlank()) {
            return SignalRouteProfile(
                canLaunch = false,
                selectedPackage = null,
                supportsCall = false,
                reason = "No number supplied for Signal"
            )
        }

        val installed = runCatching {
            context.packageManager.getPackageInfo(packageName, PackageManager.MATCH_ALL)
            true
        }.getOrDefault(false)

        return if (!installed) {
            SignalRouteProfile(
                canLaunch = false,
                selectedPackage = packageName,
                supportsCall = false,
                reason = "Signal app is not installed"
            )
        } else {
            SignalRouteProfile(
                canLaunch = true,
                selectedPackage = packageName,
                supportsCall = false,
                reason = "Signal installed"
            )
        }
    }
}
