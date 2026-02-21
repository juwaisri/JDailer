package com.jdailer.presentation.dialer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdailer.feature.dialer.domain.model.DialerSuggestion
import com.jdailer.feature.dialer.domain.usecase.ObserveDialSuggestionsUseCase
import com.jdailer.feature.voip.domain.model.CallRouteResult
import com.jdailer.feature.voip.domain.usecase.PlaceTelecomCallUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
class DialerViewModel(
    private val observeDialSuggestionsUseCase: ObserveDialSuggestionsUseCase,
    private val placeTelecomCallUseCase: PlaceTelecomCallUseCase
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val suggestions = _query
        .debounce(75)
        .distinctUntilChanged()
        .flatMapLatest { observeDialSuggestionsUseCase(it, limit = 24) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChanged(next: String) {
        _query.value = next
    }

    fun appendDigit(digit: String) {
        if (digit == "⌫") {
            _query.value = _query.value.dropLast(1)
        } else if (_query.value.length < 64) {
            _query.value += digit
        }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun onSelectSuggestion(suggestion: DialerSuggestion): String {
        _query.value = suggestion.phoneNumber
        return suggestion.phoneNumber
    }

    suspend fun placeCall(number: String): CallRouteResult {
        return placeTelecomCallUseCase(address = number, useSipUri = false)
    }
}
