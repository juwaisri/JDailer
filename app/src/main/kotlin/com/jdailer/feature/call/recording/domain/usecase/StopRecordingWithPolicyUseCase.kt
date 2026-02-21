package com.jdailer.feature.call.recording.domain.usecase

import com.jdailer.feature.call.recording.CallRecordingManager

class StopRecordingWithPolicyUseCase(
    private val manager: CallRecordingManager
) {
    suspend operator fun invoke(sessionId: String): Result<String?> = manager.stopRecording(sessionId)
}
