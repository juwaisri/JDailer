package com.jdailer.core.privacy

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class RecordingPrivacyPolicy(
    val recordingEnabled: Boolean = false,
    val requireExplicitConsent: Boolean = true,
    val autoDeleteAfterDays: Int = 30,
    val allowCloudBackup: Boolean = false,
    val redactMetadata: Boolean = true,
    val notifyOnRecordingStart: Boolean = true
)

private val Context.recordingPrivacyDataStore by preferencesDataStore(name = "recording_privacy_prefs")

class RecordingPrivacyPolicyStore(
    private val context: Context
) {
    private val datastore = context.recordingPrivacyDataStore

    private object Keys {
        val RECORDING_ENABLED = booleanPreferencesKey("recording_enabled")
        val REQUIRE_CONSENT = booleanPreferencesKey("require_explicit_consent")
        val AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_after_days")
        val ALLOW_CLOUD_BACKUP = booleanPreferencesKey("allow_cloud_backup")
        val REDACT_METADATA = booleanPreferencesKey("redact_metadata")
        val NOTIFY_ON_RECORDING = booleanPreferencesKey("notify_on_recording_start")
    }

    val policy: Flow<RecordingPrivacyPolicy> = datastore.data.map { prefs ->
        RecordingPrivacyPolicy(
            recordingEnabled = prefs[Keys.RECORDING_ENABLED] ?: false,
            requireExplicitConsent = prefs[Keys.REQUIRE_CONSENT] ?: true,
            autoDeleteAfterDays = prefs[Keys.AUTO_DELETE_DAYS] ?: 30,
            allowCloudBackup = prefs[Keys.ALLOW_CLOUD_BACKUP] ?: false,
            redactMetadata = prefs[Keys.REDACT_METADATA] ?: true,
            notifyOnRecordingStart = prefs[Keys.NOTIFY_ON_RECORDING] ?: true,
        )
    }

    suspend fun setRecordingEnabled(enabled: Boolean) {
        datastore.edit { prefs ->
            prefs[Keys.RECORDING_ENABLED] = enabled
        }
    }

    suspend fun setRequireExplicitConsent(required: Boolean) {
        datastore.edit { prefs ->
            prefs[Keys.REQUIRE_CONSENT] = required
        }
    }

    suspend fun setAutoDeleteAfterDays(days: Int) {
        datastore.edit { prefs ->
            prefs[Keys.AUTO_DELETE_DAYS] = days.coerceAtLeast(1).coerceAtMost(365)
        }
    }

    suspend fun setAllowCloudBackup(allowed: Boolean) {
        datastore.edit { prefs ->
            prefs[Keys.ALLOW_CLOUD_BACKUP] = allowed
        }
    }

    suspend fun setRedactMetadata(redact: Boolean) {
        datastore.edit { prefs ->
            prefs[Keys.REDACT_METADATA] = redact
        }
    }

    suspend fun setNotifyOnRecordingStart(notify: Boolean) {
        datastore.edit { prefs ->
            prefs[Keys.NOTIFY_ON_RECORDING] = notify
        }
    }
}

