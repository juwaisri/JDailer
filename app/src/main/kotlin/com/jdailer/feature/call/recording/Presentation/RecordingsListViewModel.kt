package com.jdailer.feature.call.recording.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdailer.core.database.dao.CallRecordingDao
import com.jdailer.feature.call.recording.CallRecordingManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordingsListViewModel(
    private val dao: CallRecordingDao,
    private val callRecordingManager: CallRecordingManager
) : ViewModel() {
    val recordings = dao.observeAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun clearRecordings() {
        viewModelScope.launch {
            callRecordingManager.clearRecordings()
        }
    }
}
