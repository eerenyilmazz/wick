package com.wick.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.event.AppEventBus
import com.wick.core.data.session.SessionRepository
import com.wick.core.model.AppEvent
import com.wick.core.model.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val appEventBus: AppEventBus,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            appEventBus.events.collect { event ->
                when (event) {
                    AppEvent.SessionStarted, AppEvent.SessionLocked,
                    AppEvent.MatchCreated, AppEvent.MessageReceived -> load()
                    else -> Unit
                }
            }
        }
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            _uiState.value = when (val result = sessionRepository.listConversations()) {
                is AppResult.Success -> _uiState.value.copy(isLoading = false, conversations = result.data)
                is AppResult.Failure -> _uiState.value.copy(isLoading = false, error = result.error)
            }
        }
    }
}
