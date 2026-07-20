package com.wick.feature.profile.notifications

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.notification.NotificationPreferencesRepository
import com.wick.core.model.AppResult
import com.wick.core.model.NotificationPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NotificationField {
    data object NewMatches : NotificationField
    data object Invites : NotificationField
    data object Messages : NotificationField
    data object Reminders : NotificationField
    data object Promos : NotificationField
}

@Immutable
data class NotificationPrefsState(
    val isLoading: Boolean = true,
    val newMatches: Boolean = true,
    val invites: Boolean = true,
    val messages: Boolean = true,
    val reminders: Boolean = false,
    val promos: Boolean = false,
)

@HiltViewModel
class NotificationPreferencesViewModel @Inject constructor(
    private val repository: NotificationPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationPrefsState())
    val state: StateFlow<NotificationPrefsState> = _state.asStateFlow()

    private var persistJob: Job? = null

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            when (val result = repository.getPreferences()) {
                is AppResult.Success -> _state.value = result.data.toState()
                is AppResult.Failure -> _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun toggle(field: NotificationField, value: Boolean) {
        val current = _state.value
        val updated = when (field) {
            is NotificationField.NewMatches -> current.copy(newMatches = value)
            is NotificationField.Invites -> current.copy(invites = value)
            is NotificationField.Messages -> current.copy(messages = value)
            is NotificationField.Reminders -> current.copy(reminders = value)
            is NotificationField.Promos -> current.copy(promos = value)
        }
        _state.value = updated
        schedulePersist(updated)
    }

    private fun schedulePersist(state: NotificationPrefsState) {
        persistJob?.cancel()
        persistJob = viewModelScope.launch {
            delay(500)
            repository.updatePreferences(
                NotificationPreferences(
                    newMatches = state.newMatches,
                    invites = state.invites,
                    messages = state.messages,
                    reminders = state.reminders,
                    promos = state.promos,
                ),
            )
        }
    }

    private fun NotificationPreferences.toState() = NotificationPrefsState(
        isLoading = false,
        newMatches = newMatches,
        invites = invites,
        messages = messages,
        reminders = reminders,
        promos = promos,
    )
}
