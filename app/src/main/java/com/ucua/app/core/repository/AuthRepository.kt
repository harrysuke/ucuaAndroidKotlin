package com.ucua.app.core.repository

import com.ucua.app.core.model.ApiError
import com.ucua.app.core.model.ForgotRequest
import com.ucua.app.core.model.ForgotResponse
import com.ucua.app.core.model.LoginRequest
import com.ucua.app.core.model.LoginResponse
import com.ucua.app.core.model.RegisterRequest
import com.ucua.app.core.model.RegisterResponse
import com.ucua.app.core.model.User
import com.ucua.app.core.network.ApiService
import com.ucua.app.core.security.TokenStore
import com.ucua.app.core.util.Resource
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.Response

class AuthRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) {

    val sessionToken: Flow<String?> get() = tokenStore.token
    val currentUser: Flow<User?> get() = tokenStore.user
    val isLoggedIn: Flow<Boolean> get() = tokenStore.isLoggedIn

    suspend fun login(staffId: String, password: String): Resource<User> =
        withContext(Dispatchers.IO) {
            val normalized = staffId.trim()
            if (normalized.isEmpty()) return@withContext Resource.Error("Staff ID is required")
            if (password.isEmpty()) return@withContext Resource.Error("Password is required")

            try {
                val response = api.login(LoginRequest(normalized, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.valid && body.token != null && body.user != null) {
                        tokenStore.saveSession(body.token, body.user)
                        Resource.Success(body.user)
                    } else {
                        Resource.Error(body?.error ?: "Invalid credentials")
                    }
                } else {
                    Resource.Error(
                        parseError(response) ?: "Login failed (${response.code()})",
                        response.code(),
                    )
                }
            } catch (e: IOException) {
                Resource.Error("Network error. Check your connection and API address.")
            } catch (e: Exception) {
                Resource.Error("Unexpected response from the server. Please try again.")
            }
        }

    suspend fun register(request: RegisterRequest): Resource<RegisterResponse> =
        withContext(Dispatchers.IO) {
            if (request.staffId.isBlank()) return@withContext Resource.Error("Staff ID is required")
            if (request.name.isBlank()) return@withContext Resource.Error("Full name is required")
            if (request.password.length < 6) {
                return@withContext Resource.Error("Password must be at least 6 characters")
            }
            try {
                val response = api.register(request)
                if (response.isSuccessful && response.body()?.registered == true) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error(
                        parseRegisterError(response) ?: "Registration failed (${response.code()})",
                        response.code(),
                    )
                }
            } catch (e: IOException) {
                Resource.Error("Network error. Check your connection and try again.")
            } catch (e: Exception) {
                Resource.Error("Unexpected error: ${e.message}")
            }
        }

    suspend fun requestPasswordReset(staffId: String): Resource<String> =
        withContext(Dispatchers.IO) {
            if (staffId.isBlank()) return@withContext Resource.Error("Staff ID is required")
            try {
                val response = api.forgotPassword(ForgotRequest(staffId.trim()))
                val body = response.body()
                if (response.isSuccessful && body?.requested == true) {
                    Resource.Success(body.message ?: "Recovery request submitted.")
                } else {
                    Resource.Error(body?.error ?: "Could not submit the request (${response.code()})")
                }
            } catch (e: IOException) {
                Resource.Error("Network error. Check your connection and try again.")
            } catch (e: Exception) {
                Resource.Error("Unexpected error: ${e.message}")
            }
        }

    suspend fun logout() {
        tokenStore.clear()
    }

    private fun parseError(response: Response<LoginResponse>): String? =
        decodeError(response.errorBody()?.string())

    private fun parseRegisterError(response: Response<RegisterResponse>): String? =
        decodeError(response.errorBody()?.string()) ?: response.body()?.error

    private fun decodeError(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            Json { ignoreUnknownKeys = true }.decodeFromString<ApiError>(raw).error
        } catch (e: Exception) {
            null
        }
    }
}
