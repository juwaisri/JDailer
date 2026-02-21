package com.jdailer.feature.call.recording.domain.usecase

import com.jdailer.feature.call.recording.CallRecordingManager
import com.jdailer.feature.call.recording.CallSession

class StartRecordingWithConsentUseCase(
    private val validator: ValidateRecordingPolicyUseCase,
    private val recordingManager: CallRecordingManager
) {
    suspend operator fun invoke(session: CallSession, userConsented: Boolean): Result<String> {
        val sessionWithConsent = session.copy(userConsented = userConsented)
        val decision = validator(sessionWithConsent)

        if (!decision.canStart) {
            return Result.failure(SecurityException(decision.reason))
        }

        return recordingManager.startRecording(sessionWithConsent)
    }
}
