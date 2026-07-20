package com.wick.feature.profile.notifications

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.notification.InboxNotificationRepository
import com.wick.core.model.AppResult
import com.wick.core.model.InboxNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 20

@Immutable
data class NotificationsUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val items: List<InboxNotification> = emptyList(),
    val hasMore: Boolean = false,
    val hasError: Boolean = false,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: InboxNotificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    private var currentPage = 0

    init {
        load()
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
            _state.value = _state.value.copy(
                items = _state.value.items.map { if (it.id == id) it.copy(read = true) else it },
            )
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            when (repository.markAllRead()) {
                is AppResult.Success -> _state.value = _state.value.copy(
                    items = _state.value.items.map { it.copy(read = true) },
                )
                is AppResult.Failure -> _state.value = _state.value.copy(hasError = true)
            }
        }
    }

    fun retry() {
        currentPage = 0
        load()
    }

    fun loadMore() {
        if (_state.value.isLoadingMore || !_state.value.hasMore) return
        _state.value = _state.value.copy(isLoadingMore = true)
        viewModelScope.launch {
            when (val result = repository.getInbox(page = currentPage, size = PAGE_SIZE)) {
                is AppResult.Success -> {
                    currentPage++
                    _state.value = _state.value.copy(
                        isLoadingMore = false,
                        items = _state.value.items + result.data,
                        hasMore = result.data.size >= PAGE_SIZE,
                    )
                }
                is AppResult.Failure -> _state.value = _state.value.copy(
                    isLoadingMore = false,
                )
            }
        }
    }

    private fun load() {
        currentPage = 0
        viewModelScope.launch {
            _state.value = NotificationsUiState(isLoading = true)
            when (val result = repository.getInbox(page = 0, size = PAGE_SIZE)) {
                is AppResult.Success -> {
                    currentPage = 1
                    _state.value = NotificationsUiState(
                        isLoading = false,
                        items = result.data,
                        hasMore = result.data.size >= PAGE_SIZE,
                    )
                }
                is AppResult.Failure -> _state.value = NotificationsUiState(
                    isLoading = false,
                    hasError = true,
                )
            }
        }
    }
}
