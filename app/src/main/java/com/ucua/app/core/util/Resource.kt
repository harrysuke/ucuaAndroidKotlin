package com.ucua.app.core.util

/**
 * Standard result wrapper used across repositories and ViewModels.
 */
sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>
    data object Loading : Resource<Nothing>
}
