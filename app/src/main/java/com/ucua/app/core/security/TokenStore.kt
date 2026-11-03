package com.ucua.app.core.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ucua.app.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "ucua_session")

/**
 * Persists the bearer token and the cached user profile in Preferences DataStore.
 * Exposes them as safe Flows that never throw.
 */
class TokenStore(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_STAFF_ID = stringPreferencesKey("staffId")
        private val KEY_NAME = stringPreferencesKey("name")
        private val KEY_PHONE = stringPreferencesKey("phoneNo")
        private val KEY_DEPARTMENT = stringPreferencesKey("department")
        private val KEY_DESIGNATION = stringPreferencesKey("designation")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_ACCESS_LEVEL = stringPreferencesKey("accessLevel")
        private val KEY_ROLE = stringPreferencesKey("roleName")
        private val KEY_COMPANY = stringPreferencesKey("companyId")
    }

    val token: Flow<String?> = context.tokenDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> prefs[KEY_TOKEN] }

    val user: Flow<User?> = context.tokenDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val staffId = prefs[KEY_STAFF_ID] ?: return@map null
            User(
                staffId = staffId,
                name = prefs[KEY_NAME] ?: "",
                phoneNo = prefs[KEY_PHONE],
                department = prefs[KEY_DEPARTMENT],
                designation = prefs[KEY_DESIGNATION],
                email = prefs[KEY_EMAIL],
                accessLevel = prefs[KEY_ACCESS_LEVEL]?.toIntOrNull() ?: 1,
                roleName = prefs[KEY_ROLE],
                companyId = prefs[KEY_COMPANY]?.toIntOrNull(),
            )
        }

    val isLoggedIn: Flow<Boolean> = token.map { token -> !token.isNullOrBlank() }

    suspend fun currentToken(): String? = token.first()

    suspend fun saveSession(token: String, user: User) {
        context.tokenDataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_STAFF_ID] = user.staffId
            prefs[KEY_NAME] = user.name
            prefs[KEY_PHONE] = user.phoneNo ?: ""
            prefs[KEY_DEPARTMENT] = user.department ?: ""
            prefs[KEY_DESIGNATION] = user.designation ?: ""
            prefs[KEY_EMAIL] = user.email ?: ""
            prefs[KEY_ACCESS_LEVEL] = user.accessLevel.toString()
            prefs[KEY_ROLE] = user.roleName ?: ""
            prefs[KEY_COMPANY] = user.companyId?.toString() ?: ""
        }
    }

    suspend fun clear() {
        context.tokenDataStore.edit { prefs -> prefs.clear() }
    }
}
