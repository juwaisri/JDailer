package com.jdailer.feature.call.recording

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.jdailer.core.privacy.RecordingPrivacyPolicy
import com.jdailer.core.privacy.RecordingPrivacyPolicyStore
import com.jdailer.core.database.dao.CallRecordingDao
import com.jdailer.core.database.entity.CallRecordingEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.Locale

private const val RECORDING_CHANNEL_ID = "call_recording"
private const val RECORDING_NOTIFICATION_ID = 0xC001
private const val RECORDING_DIR = "call_recordings"

class DefaultCallRecordingManager(
    private val context: Context,
    private val dao: CallRecordingDao,
    private val privacyPolicyStore: RecordingPrivacyPolicyStore
) : CallRecordingManager {
    private var activeStartTimeMs: Long = 0L
    private var recorder: MediaRecorder? = null
    private var activeSessionId: String? = null

    @Suppress("DEPRECATION")
    override suspend fun startRecording(session: CallSession): Result<String> {
        return runCatching {
            val policy = privacyPolicyStore.policy.first()

            ensureRecordAudioPermission()
            if (!policy.recordingEnabled) {
                throw SecurityException("Call recording disabled by privacy policy")
            }
            if (policy.requireExplicitConsent && !session.userConsented) {
                throw SecurityException("User consent is required for call recording")
            }
            if (session.number.isNullOrBlank()) {
                throw IllegalArgumentException("Call number is required for recording metadata")
            }

            cleanupExpiredFiles(policy.autoDeleteAfterDays)

            val sessionId = java.util.UUID.randomUUID().toString()
            val outputDirectory = resolveRecordingDirectory(policy)
            outputDirectory.mkdirs()
            val outputFile = File.createTempFile("call_recording_$sessionId", ".m4a", outputDirectory)

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_UPLINK)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            activeSessionId = sessionId
            activeStartTimeMs = System.currentTimeMillis()

            maybeNotifyRecordingState(sessionId, policy, active = true)

            dao.upsert(
                CallRecordingEntity(
                    recordingId = sessionId,
                    callId = session.callId,
                    timestamp = System.currentTimeMillis(),
                    callerNumber = formatCallerNumber(policy, session.number),
                    direction = session.direction,
                    filePath = outputFile.absolutePath,
                    durationMs = 0,
                    fileSizeBytes = 0,
                    consented = session.userConsented
                )
            )
            sessionId
        }.onFailure {
            recorder?.apply {
                runCatching { stop() }
                runCatching { reset() }
                runCatching { release() }
            }
            recorder = null
            activeSessionId = null
            activeStartTimeMs = 0L
            maybeNotifyRecordingState(null, null, active = false)
        }
    }

    override suspend fun stopRecording(sessionId: String): Result<String?> {
        return runCatching {
            if (activeSessionId != sessionId) return@runCatching null
            val currentStartTime = activeStartTimeMs

            recorder?.apply {
                runCatching { stop() }
                reset()
                release()
            }
            recorder = null
            activeSessionId = null
            activeStartTimeMs = 0L
            maybeNotifyRecordingState(sessionId, null, active = false)

            val durationMs = System.currentTimeMillis() - currentStartTime
            val events = dao.observeAll().first()
            val target = events.firstOrNull { it.recordingId == sessionId }
            if (target != null) {
                dao.updateDuration(
                    sessionId,
                    durationMs.coerceAtLeast(0),
                    File(target.filePath).let { if (it.exists()) it.length() else 0 }
                )
            }
            sessionId
        }
    }

    override suspend fun clearRecordings(): Result<Unit> {
        return runCatching {
            val events = dao.observeAll().first()
            events.forEach { event ->
                runCatching { File(event.filePath).delete() }
            }
            dao.clearAll()
            maybeNotifyRecordingState("", null, active = false)
        }
    }

    override suspend fun listRecordingFiles(): Result<List<File>> = runCatching {
        val events = dao.observeAll().first()
        val files = mutableListOf<File>()
        events.forEach { event ->
            val file = File(event.filePath)
            if (file.exists()) {
                files.add(file)
            } else {
                dao.deleteRecording(event.recordingId)
            }
        }
        files
    }

    private suspend fun cleanupExpiredFiles(retentionDays: Int) {
        val retention = retentionDays.coerceAtLeast(0)
        if (retention == 0) {
            return
        }

        withContext(kotlinx.coroutines.Dispatchers.IO) {
            val cutoffMs = System.currentTimeMillis() - (retention.toLong() * 24L * 60 * 60 * 1000)
            val events = dao.observeAll().first()
            events.filter { it.timestamp < cutoffMs }
                .forEach { event ->
                    runCatching { File(event.filePath).delete() }
                    dao.deleteRecording(event.recordingId)
                }
        }
    }

    private fun resolveRecordingDirectory(policy: RecordingPrivacyPolicy): File {
        val base = if (policy.allowCloudBackup) {
            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        } else {
            context.filesDir
        } ?: context.filesDir
        return File(base, RECORDING_DIR)
    }

    private fun formatCallerNumber(policy: RecordingPrivacyPolicy, rawNumber: String): String {
        return if (policy.redactMetadata) {
            "redacted_${hash(rawNumber)}"
        } else {
            rawNumber
        }
    }

    private fun hash(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .take(12)
            .toByteArray()
        return bytes.joinToString("") { byte -> String.format(Locale.US, "%02x", byte) }
    }

    private fun ensureRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("RECORD_AUDIO permission is required for call recording")
        }
    }

    private fun maybeNotifyRecordingState(sessionId: String?, policy: RecordingPrivacyPolicy?, active: Boolean) {
        val resolvedPolicy = policy ?: return
        if (!resolvedPolicy.notifyOnRecordingStart) {
            return
        }

        val manager = context.getSystemService<NotificationManager>() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RECORDING_CHANNEL_ID,
                "Call Recording",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        if (!active) {
            runCatching { manager.cancel(RECORDING_NOTIFICATION_ID) }
            return
        }

        val label = if (sessionId.isNullOrBlank()) {
            "Active"
        } else {
            "Session ${sessionId.take(8)}"
        }
        val notification = NotificationCompat.Builder(context, RECORDING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentTitle("Recording call")
            .setContentText(label)
            .build()

        runCatching { manager.notify(RECORDING_NOTIFICATION_ID, notification) }
    }
}
