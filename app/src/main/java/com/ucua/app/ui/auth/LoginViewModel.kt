package com.ucua.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucua.app.core.repository.AuthRepository
import com.ucua.app.core.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val staffId: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val staffIdError: String? = null,
    val passwordError: String? = null,
    val loggedIn: Boolean = false,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onStaffIdChange(value: String) {
        _uiState.update { it.copy(staffId = value, staffIdError = null, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, error = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun login() {
        val state = _uiState.value
        val staffIdError = if (state.staffId.isBlank()) "Enter your Staff ID" else null
        val passwordError = if (state.password.isBlank()) "Enter your password" else null
        if (staffIdError != null || passwordError != null) {
            _uiState.update { it.copy(staffIdError = staffIdError, passwordError = passwordError) }
            return
        }
        if (state.loading) return

        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.login(state.staffId, state.password)) {
                is Resource.Success -> _uiState.update { it.copy(loading = false, loggedIn = true) }
                is Resource.Error -> _uiState.update {
                    it.copy(loading = false, error = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
