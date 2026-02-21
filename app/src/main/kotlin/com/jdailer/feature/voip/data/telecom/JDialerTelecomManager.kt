package com.jdailer.feature.voip.data.telecom

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import com.jdailer.feature.voip.data.telecom.JDialerConnectionService
import com.jdailer.feature.voip.domain.model.CallRouteResult
import com.jdailer.feature.voip.domain.model.CallRouteType
import com.jdailer.feature.voip.domain.repository.VoipRepository
import timber.log.Timber

class JDialerTelecomManager(
    private val context: Context,
    private val voipRepository: VoipRepository
) {
    private val telecomManager: TelecomManager =
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    @RequiresApi(Build.VERSION_CODES.O)
    fun registerSelfManagedPhoneAccount() {
        val component = ComponentName(context, JDialerConnectionService::class.java)
        val handle = PhoneAccountHandle(component, "jdailer_voip")

        val existing = telecomManager.callCapablePhoneAccounts
            .any { it == handle }
        if (existing) {
            return
        }

        val account = PhoneAccount.builder(handle, "JDialer VoIP")
            .setAddress(Uri.parse("tel:"))
            .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
            .build()
        telecomManager.registerPhoneAccount(account)
    }

    fun launchOutgoingCall(address: String, useSipUri: Boolean = false): CallRouteResult {
        val normalized = normalizeAddress(address)
        val uri = if (useSipUri) {
            Uri.parse("sip:$normalized")
        } else {
            Uri.parse("tel:$normalized")
        }

        if (normalized.isBlank()) {
            return CallRouteResult(
                number = address,
                routeType = CallRouteType.FAILED,
                usedSipUri = useSipUri,
                message = "No number provided"
            )
        }

        val useTelecom = hasCallPermission() && canRouteThroughTelecom()
        if (useTelecom) {
            runCatching {
                telecomManager.placeCall(uri, Bundle())
            }.onSuccess {
                return CallRouteResult(
                    number = normalized,
                    routeType = CallRouteType.TELECOM,
                    usedSipUri = useSipUri
                )
            }.onFailure { throwable ->
                Timber.w(throwable, "Telecom placeCall failed for $normalized")
            }
        }

        return fallbackDialerLaunch(uri, normalized, useSipUri)
    }

    fun isDefaultDialer(): Boolean {
        return telecomManager.defaultDialerPackage == context.packageName
    }

    fun isSelfManagedConfigured(): Boolean {
        val handle = PhoneAccountHandle(ComponentName(context, JDialerConnectionService::class.java), "jdailer_voip")
        return telecomManager.getPhoneAccount(handle) != null
    }

    private fun hasCallPermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    }

    private fun canRouteThroughTelecom(): Boolean = isDefaultDialer() && isSelfManagedConfigured()

    private fun fallbackDialerLaunch(uri: Uri, normalized: String, useSipUri: Boolean): CallRouteResult {
        val intent = Intent(Intent.ACTION_DIAL, uri)
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        return runCatching {
            context.startActivity(intent)
            CallRouteResult(
                number = normalized,
                routeType = CallRouteType.FALLBACK_DIAL,
                usedSipUri = useSipUri
            )
        }.getOrElse { exception ->
            Timber.w(exception, "Fallback dial launch failed for $normalized")
            CallRouteResult(
                number = normalized,
                routeType = CallRouteType.FAILED,
                usedSipUri = useSipUri,
                message = exception.message.orEmpty()
            )
        }
    }

    private fun normalizeAddress(raw: String): String {
        return raw.filter { it.isDigit() || it == '+' }.ifBlank { raw.trim() }
    }
}
