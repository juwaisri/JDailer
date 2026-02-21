package com.jdailer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdailer.core.privacy.RecordingPrivacyPolicy
import com.jdailer.core.privacy.RecordingPrivacyPolicyStore
import com.jdailer.feature.call.recording.CallRecordingManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val recordingPrivacyPolicyStore: RecordingPrivacyPolicyStore,
    private val callRecordingManager: CallRecordingManager
) : ViewModel() {
    val policy: Flow<RecordingPrivacyPolicy> = recordingPrivacyPolicyStore.policy

    val uiState = recordingPrivacyPolicyStore.policy.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordingPrivacyPolicy()
    )

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: Flow<String?> = _actionMessage.asStateFlow()

    fun setRecordingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            recordingPrivacyPolicyStore.setRecordingEnabled(enabled)
        }
    }

    fun setRequireExplicitConsent(enabled: Boolean) {
        viewModelScope.launch {
            recordingPrivacyPolicyStore.setRequireExplicitConsent(enabled)
        }
    }

    fun setAutoDeleteAfterDays(days: Int) {
        viewModelScope.launch {
            recordingPrivacyPolicyStore.setAutoDeleteAfterDays(days)
        }
    }

    fun setAllowCloudBackup(allowed: Boolean) {
        viewModelScope.launch {
            recordingPrivacyPolicyStore.setAllowCloudBackup(allowed)
        }
    }

    fun setRedactMetadata(redact: Boolean) {
        viewModelScope.launch {
            recordingPrivacyPolicyStore.setRedactMetadata(redact)
        }
    }

    fun setNotifyOnRecording(enabled: Boolean) {
        viewModelScope.launch {
            recordingPrivacyPolicyStore.setNotifyOnRecordingStart(enabled)
        }
    }

    fun clearRecordings() {
        viewModelScope.launch {
            _actionMessage.update { "Clearing call recordings" }
            val result = callRecordingManager.clearRecordings()
            if (result.isSuccess) {
                _actionMessage.update { "All recordings removed" }
            } else {
                _actionMessage.update {
                    result.exceptionOrNull()?.message ?: "Unable to clear recordings"
                }
            }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.update { null }
    }
}
