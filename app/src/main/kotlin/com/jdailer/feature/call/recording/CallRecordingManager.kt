package com.jdailer.feature.call.recording

import java.io.File
import java.util.UUID

interface CallRecordingManager {
    suspend fun startRecording(session: CallSession): Result<String>
    suspend fun stopRecording(sessionId: String): Result<String?>
    suspend fun clearRecordings(): Result<Unit>
    suspend fun listRecordingFiles(): Result<List<File>>
}
