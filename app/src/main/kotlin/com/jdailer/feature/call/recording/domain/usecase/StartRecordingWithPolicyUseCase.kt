package com.jdailer.feature.call.recording.domain.usecase

import com.jdailer.core.privacy.RecordingPrivacyPolicyStore
import com.jdailer.feature.call.recording.CallRecordingManager
import com.jdailer.feature.call.recording.CallSession
import kotlinx.coroutines.flow.first

class StartRecordingWithPolicyUseCase(
    private val manager: CallRecordingManager,
    private val policyStore: RecordingPrivacyPolicyStore
) {
    suspend operator fun invoke(session: CallSession): Result<String> {
        val policy = policyStore.policy.first()
        if (!policy.recordingEnabled) {
            return Result.failure(SecurityException("Call recording disabled by policy"))
        }
        if (policy.requireExplicitConsent && !session.userConsented) {
            return Result.failure(SecurityException("Caller consent required for recording"))
        }

        return manager.startRecording(session)
    }
}
