package com.wick.feature.profile.wallet

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.token.TokenRepository
import com.wick.core.model.AppResult
import com.wick.core.model.TokenTransaction as TokenTx
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 20

@Immutable
data class TransactionHistoryUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val groups: List<TxGroup> = emptyList(),
    val totalBalance: String = "—",
    val monthlyDelta: String = "—",
    val hasMore: Boolean = false,
    val hasError: Boolean = false,
)

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private val allTxs = mutableListOf<TokenTx>()

    init {
        load()
    }

    fun retry() {
        currentPage = 0
        allTxs.clear()
        load()
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        _uiState.value = _uiState.value.copy(isLoadingMore = true)
        viewModelScope.launch {
            when (val result = tokenRepository.getTransactions(page = currentPage, size = PAGE_SIZE)) {
                is AppResult.Success -> {
                    currentPage++
                    allTxs.addAll(result.data)
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        groups = allTxs.toGroups(),
                        hasMore = result.data.size >= PAGE_SIZE,
                    )
                }
                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                )
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = TransactionHistoryUiState(isLoading = true)
            when (val result = tokenRepository.getTransactions(page = 0, size = PAGE_SIZE)) {
                is AppResult.Success -> {
                    currentPage = 1
                    allTxs.clear()
                    allTxs.addAll(result.data)
                    val now = java.time.Instant.now().toString().take(7)
                    val totalEarned = allTxs.filter { it.amount > 0 }.sumOf { it.amount }
                    val monthlyDelta = allTxs.filter { it.createdAt.take(7) == now }.sumOf { it.amount }
                    _uiState.value = TransactionHistoryUiState(
                        isLoading = false,
                        groups = allTxs.toGroups(),
                        totalBalance = totalEarned.toString(),
                        monthlyDelta = if (monthlyDelta >= 0) "+$monthlyDelta" else "$monthlyDelta",
                        hasMore = result.data.size >= PAGE_SIZE,
                    )
                }
                is AppResult.Failure -> _uiState.value = TransactionHistoryUiState(
                    isLoading = false,
                    hasError = true,
                )
            }
        }
    }

    private fun List<TokenTx>.toGroups(): List<TxGroup> =
        groupBy { it.createdAt.take(10) }
            .entries
            .sortedByDescending { it.key }
            .map { (date, items) -> TxGroup(label = date, items = items.map { it.toUiModel() }) }

    private fun TokenTx.toUiModel(): TokenTransaction {
        val type = when {
            reason == "PURCHASED" -> TxType.PURCHASE
            amount < 0 -> TxType.SPEND
            else -> TxType.BONUS
        }
        return TokenTransaction(
            id = id,
            reason = reason,
            timeLabel = createdAt.take(16).replace("T", " "),
            amount = amount,
            type = type,
        )
    }
}
