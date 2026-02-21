package com.jdailer.presentation.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdailer.core.common.result.AppResult
import com.jdailer.feature.call.spam.domain.usecase.BlockCallerUseCase
import com.jdailer.feature.contacts.domain.model.UnifiedContact
import com.jdailer.feature.contacts.domain.repository.ContactsRepository
import com.jdailer.feature.contacts.domain.usecase.ResolveContactEmailsUseCase
import com.jdailer.feature.quickactions.domain.model.SmartAction
import com.jdailer.feature.quickactions.domain.repository.SmartContactCardRepository
import com.jdailer.feature.voip.domain.model.CallRouteResult
import com.jdailer.feature.voip.domain.usecase.PlaceTelecomCallUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsViewModel(
    private val contactsRepository: ContactsRepository,
    private val smartContactCardRepository: SmartContactCardRepository,
    private val blockCallerUseCase: BlockCallerUseCase,
    private val resolveContactEmailsUseCase: ResolveContactEmailsUseCase,
    private val placeTelecomCallUseCase: PlaceTelecomCallUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactsUiState())

    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    val contacts: StateFlow<List<UnifiedContact>> = uiState
        .map { it.query }
        .debounce(80)
        .flatMapLatest { query -> contactsRepository.observeContacts(query, 500) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchChanged(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun syncFromDevice() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSyncing = true,
                    syncMessage = "Syncing contacts..."
                )
            }

            when (val result = contactsRepository.syncFromDevice()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastSyncedAtMs = System.currentTimeMillis(),
                            syncMessage = "Contacts synced"
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncMessage = result.throwable.message ?: "Contacts sync failed"
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isSyncing = false, syncMessage = null) }
                }
            }
        }
    }

    fun clearSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }

    suspend fun resolveQuickActions(contact: UnifiedContact): List<SmartAction> =
        smartContactCardRepository.resolveActions(contact)

    suspend fun resolveQuickTags(contactId: Long): List<String> =
        smartContactCardRepository.resolveTags(contactId)

    suspend fun resolveLatestNote(contactId: Long): String? =
        smartContactCardRepository.resolveLatestNote(contactId)

    suspend fun resolvePrimaryEmail(contactId: Long): String? =
        resolveContactEmailsUseCase(contactId).firstOrNull()

    fun blockNumber(number: String) {
        viewModelScope.launch {
            blockCallerUseCase(number, true)
        }
    }

    suspend fun placeCall(number: String): CallRouteResult {
        return placeTelecomCallUseCase(address = number, useSipUri = false)
    }
}

data class ContactsUiState(
    val query: String = "",
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val lastSyncedAtMs: Long? = null
)
