package com.jdailer.feature.voip.domain.usecase

import com.jdailer.feature.voip.data.telecom.JDialerTelecomManager
import com.jdailer.feature.voip.domain.model.CallTransportDecision
import com.jdailer.feature.voip.domain.model.CallTransportType
import com.jdailer.feature.voip.domain.model.TelecomEligibilityPolicy
import com.jdailer.feature.voip.domain.repository.SipProfileRepository
import kotlinx.coroutines.flow.first

class ResolveCallTransportUseCase(
    private val telecomManager: JDialerTelecomManager,
    private val resolveTelecomRoleUseCase: ResolveTelecomRoleUseCase,
    private val sipProfileRepository: SipProfileRepository,
    private val policy: TelecomEligibilityPolicy = TelecomEligibilityPolicy()
) {
    suspend operator fun invoke(rawAddress: String, preferSip: Boolean = false): CallTransportDecision {
        val address = rawAddress.filter { it.isDigit() || it == '+' }
        if (address.isBlank() && policy.requireValidAddress) {
            return CallTransportDecision(
                address = rawAddress,
                transportType = CallTransportType.BLOCKED,
                reason = "Invalid dial target",
                routed = false
            )
        }

        val sipProfile = sipProfileRepository.observeProfile().first()
        val hasSipProfile = sipProfile.enabled && sipProfile.domain.isNotBlank() && sipProfile.username.isNotBlank()

        if (preferSip && policy.allowSipWhenEnabled && hasSipProfile) {
            return CallTransportDecision(
                address = address,
                transportType = CallTransportType.SIP,
                reason = "Using SIP profile ${sipProfile.profileId}",
                useSipUri = true,
                routed = false
            )
        }

        val isDefaultDialer = resolveTelecomRoleUseCase()
        if (policy.requireDefaultDialer && !isDefaultDialer) {
            return if (policy.allowFallbackDial) {
                CallTransportDecision(
                    address = address,
                    transportType = CallTransportType.FALLBACK_DIAL,
                    reason = "Not default dialer role",
                    routed = false
                )
            } else {
                CallTransportDecision(
                    address = address,
                    transportType = CallTransportType.BLOCKED,
                    reason = "App not registered as default dialer",
                    routed = false
                )
            }
        }

        if (telecomManager.isSelfManagedConfigured() || policy.allowTelecomFallback) {
            return CallTransportDecision(
                address = address,
                transportType = CallTransportType.TELECOM,
                reason = if (telecomManager.isSelfManagedConfigured()) {
                    "Using Telecom ConnectionService"
                } else {
                    "Using direct fallback telecom dial"
                },
                useSipUri = false,
                routed = false
            )
        }

        return CallTransportDecision(
            address = address,
            transportType = CallTransportType.FALLBACK_DIAL,
            reason = "Telecom unavailable",
            routed = false
        )
    }
}
