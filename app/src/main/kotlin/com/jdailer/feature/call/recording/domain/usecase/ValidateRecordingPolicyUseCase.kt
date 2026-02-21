package com.jdailer.feature.call.recording.domain.usecase

import com.jdailer.core.database.dao.CallRecordingDao
import com.jdailer.core.privacy.RecordingPrivacyPolicyStore
import com.jdailer.feature.call.recording.CallSession
import com.jdailer.feature.call.recording.domain.model.CallRecordingPolicy
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ValidateRecordingPolicyUseCase(
    private val policyStore: RecordingPrivacyPolicyStore,
    private val recordingDao: CallRecordingDao,
    private val policy: CallRecordingPolicy = CallRecordingPolicy()
) {
    suspend operator fun invoke(session: CallSession): com.jdailer.feature.call.recording.domain.model.RecordingPolicyDecision {
        val resolvedPolicy = policyStore.policy.first()
        if (!resolvedPolicy.recordingEnabled) {
            return com.jdailer.feature.call.recording.domain.model.RecordingPolicyDecision(
                canStart = false,
                reason = "Call recording disabled in privacy settings"
            )
        }

        val normalized = session.number.orEmpty().filter { it.isDigit() || it == '+' }
        if (normalized.length < policy.minCallerIdLength) {
            return com.jdailer.feature.call.recording.domain.model.RecordingPolicyDecision(
                canStart = false,
                reason = "Caller id too short for compliance policy"
            )
        }

        if (resolvedPolicy.requireExplicitConsent && !session.userConsented) {
            return com.jdailer.feature.call.recording.domain.model.RecordingPolicyDecision(
                canStart = false,
                reason = "Caller consent required"
            )
        }

        val todayRecords = recordingDao.observeAll().first().count { recording ->
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            recording.timestamp >= startOfDay
        }

        if (todayRecords >= policy.maxRecordingsPerDay) {
            return com.jdailer.feature.call.recording.domain.model.RecordingPolicyDecision(
                canStart = false,
                reason = "Recording limit reached for today"
            )
        }

        return com.jdailer.feature.call.recording.domain.model.RecordingPolicyDecision(canStart = true)
    }
}
