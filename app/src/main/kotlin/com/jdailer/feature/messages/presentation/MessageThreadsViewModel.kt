package com.jdailer.feature.messages.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jdailer.core.database.enums.HistorySource
import com.jdailer.feature.messages.domain.model.UnifiedMessageThread
import com.jdailer.feature.messages.domain.usecase.ObserveMessageThreadsUseCase
import com.jdailer.feature.messages.domain.usecase.SyncMessagesWithPolicyUseCase
import com.jdailer.feature.messages.domain.model.SmsMmsSyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

class MessageThreadsViewModel(
    private val observeMessageThreadsUseCase: ObserveMessageThreadsUseCase,
    private val syncMessagesWithPolicyUseCase: SyncMessagesWithPolicyUseCase
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query
    private val _sources = MutableStateFlow<Set<HistorySource>>(setOf(HistorySource.SMS, HistorySource.MMS))
    val sources: StateFlow<Set<HistorySource>> = _sources
    private val _syncMessage = MutableStateFlow<SmsMmsSyncResult?>(null)
    val syncMessage = _syncMessage.asStateFlow()

    val threads: Flow<PagingData<UnifiedMessageThread>> = combine(_query, _sources) { query, sources ->
        Pair(query, sources)
    }.flatMapLatest { (query, sources) ->
        if (sources.isEmpty()) {
            flowOf(PagingData.empty())
        } else {
            observeMessageThreadsUseCase(
                query = query.takeIf { it.isNotBlank() },
                sources = sources
            )
        }
    }.cachedIn(viewModelScope)

    fun onQueryUpdated(value: String) {
        _query.value = value
    }

    fun toggleSource(source: HistorySource) {
        val current = _sources.value.toMutableSet()
        if (current.contains(source)) {
            current.remove(source)
        } else {
            current.add(source)
        }
        if (current.isEmpty()) {
            current.add(HistorySource.SMS)
            current.add(HistorySource.MMS)
        }
        _sources.value = current
    }

    fun refresh() {
        viewModelScope.launch {
            val result = syncMessagesWithPolicyUseCase()
            _syncMessage.value = result.getOrNull()
        }
    }
}

