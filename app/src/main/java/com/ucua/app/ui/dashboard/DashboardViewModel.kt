package com.ucua.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucua.app.core.model.DashboardResponse
import com.ucua.app.core.model.User
import com.ucua.app.core.repository.AuthRepository
import com.ucua.app.core.repository.DashboardRepository
import com.ucua.app.core.util.ReportEvents
import com.ucua.app.core.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val user: User? = null,
    val data: DashboardResponse? = null,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: String? = null,
)

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
        load()
        viewModelScope.launch {
            ReportEvents.changes.collect { load(isRefresh = true) }
        }
    }

    fun load(isRefresh: Boolean = false) {
        val state = _uiState.value
        if (state.refreshing) return
        _uiState.update {
            it.copy(loading = !isRefresh && it.data == null, refreshing = isRefresh, error = null)
        }
        viewModelScope.launch {
            when (val result = dashboardRepository.load()) {
                is Resource.Success -> _uiState.update {
                    it.copy(loading = false, refreshing = false, data = result.data, error = null)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(loading = false, refreshing = false, error = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}
