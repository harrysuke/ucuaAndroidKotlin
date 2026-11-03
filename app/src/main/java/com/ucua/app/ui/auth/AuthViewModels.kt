package com.ucua.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucua.app.core.model.RegisterRequest
import com.ucua.app.core.repository.AuthRepository
import com.ucua.app.core.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val staffId: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNo: String = "",
    val department: String = "",
    val designation: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val successMessage: String? = null,
)

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun update(field: String, value: String) {
        _uiState.update { current ->
            val next = when (field) {
                "staffId" -> current.copy(staffId = value)
                "name" -> current.copy(name = value)
                "email" -> current.copy(email = value)
                "phoneNo" -> current.copy(phoneNo = value)
                "department" -> current.copy(department = value)
                "designation" -> current.copy(designation = value)
                "password" -> current.copy(password = value)
                "confirmPassword" -> current.copy(confirmPassword = value)
                else -> current
            }
            next.copy(fieldErrors = next.fieldErrors - field, error = null)
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.loading) return

        val errors = buildMap {
            if (state.staffId.isBlank()) put("staffId", "Staff ID is required")
            if (state.name.isBlank()) put("name", "Full name is required")
            if (state.password.length < 6) put("password", "Use at least 6 characters")
            if (state.confirmPassword != state.password) {
                put("confirmPassword", "Passwords do not match")
            }
            if (state.email.isNotBlank() && !state.email.contains("@")) {
                put("email", "Enter a valid email address")
            }
        }
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val request = RegisterRequest(
                staffId = state.staffId.trim(),
                name = state.name.trim(),
                password = state.password,
                email = state.email.trim().ifBlank { null },
                phoneNo = state.phoneNo.trim().ifBlank { null },
                department = state.department.trim().ifBlank { null },
                designation = state.designation.trim().ifBlank { null },
            )
            when (val result = authRepository.register(request)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        loading = false,
                        successMessage = result.data.message
                            ?: "Registration submitted for approval.",
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(loading = false, error = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}

data class ForgotUiState(
    val staffId: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)

class ForgotViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotUiState())
    val uiState: StateFlow<ForgotUiState> = _uiState.asStateFlow()

    fun onStaffIdChange(value: String) {
        _uiState.update { it.copy(staffId = value, error = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.staffId.isBlank()) {
            _uiState.update { it.copy(error = "Enter your Staff ID") }
            return
        }
        if (state.loading) return

        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.requestPasswordReset(state.staffId)) {
                is Resource.Success -> _uiState.update {
                    it.copy(loading = false, message = result.data)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(loading = false, error = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}
