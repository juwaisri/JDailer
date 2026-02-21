package com.jdailer.feature.messages.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jdailer.feature.messages.domain.model.UnifiedMessageItem
import com.jdailer.feature.messages.domain.usecase.ObserveMessageItemsUseCase
import com.jdailer.feature.messages.domain.usecase.SendMessageWithPolicyUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MessageThreadViewModel(
    private val observeMessageItemsUseCase: ObserveMessageItemsUseCase,
    private val sendMessageUseCase: SendMessageWithPolicyUseCase
) : ViewModel() {
    private val _threadId = MutableStateFlow<String?>(null)
    val threadId: StateFlow<String?> = _threadId.asStateFlow()

    private val _uiState = MutableStateFlow(MessageThreadUiState())
    val uiState: StateFlow<MessageThreadUiState> = _uiState.asStateFlow()

    val messages: Flow<PagingData<UnifiedMessageItem>> = _threadId
        .flatMapLatest { threadId ->
            threadId?.let {
                observeMessageItemsUseCase(it)
            } ?: flowOf(PagingData.empty())
        }
        .cachedIn(viewModelScope)

    fun openThread(id: String) {
        _threadId.value = id
    }

    fun sendReply(number: String, text: String, threadId: String) {
        val attachmentUris = uiState.value.attachmentUris
        viewModelScope.launch {
            if (number.isBlank() && threadId.isBlank()) return@launch

            _uiState.value = _uiState.value.copy(isSending = true, lastError = null)
            val result = sendMessageUseCase.sendSmsOrMms(number, text, attachmentUris, threadId)
            _uiState.value = _uiState.value.copy(isSending = false)
            result.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(lastError = throwable.message.orEmpty())
            }
            result.onSuccess {
                _uiState.value = _uiState.value.copy(attachmentUris = emptyList(), draftText = "")
            }
        }
    }

    fun addAttachment(uri: String) {
        if (uri.isBlank()) return

        val normalized = uiState.value.attachmentUris.filterNot { it == uri }
        _uiState.value = _uiState.value.copy(
            attachmentUris = (normalized + uri).take(8)
        )
    }

    fun removeAttachment(uri: String) {
        _uiState.value = _uiState.value.copy(
            attachmentUris = _uiState.value.attachmentUris.filterNot { it == uri }
        )
    }

    fun setDraftText(text: String) {
        _uiState.value = _uiState.value.copy(draftText = text)
    }

    fun clearLastError() {
        _uiState.value = _uiState.value.copy(lastError = null)
    }
}

data class MessageThreadUiState(
    val attachmentUris: List<String> = emptyList(),
    val draftText: String = "",
    val isSending: Boolean = false,
    val lastError: String? = null
)
