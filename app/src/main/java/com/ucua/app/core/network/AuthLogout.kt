package com.ucua.app.core.network

import com.ucua.app.core.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Clears the local session (token + cached profile) on a background coroutine,
 * then invokes [onDone] on the main thread.
 */
object AuthLogout {
    fun doLogout(scope: CoroutineScope, authRepository: AuthRepository, onDone: () -> Unit) {
        scope.launch {
            authRepository.logout()
            onDone()
        }
    }
}
